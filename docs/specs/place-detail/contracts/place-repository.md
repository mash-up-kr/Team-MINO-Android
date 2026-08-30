# 계약: 도메인 Repository (Place · PlaceComment)

**대상 스펙 경로**: `docs/specs/place-detail`

**계획서**: [../plan.md](../plan.md)

`:core:domain`이 소유하는 인터페이스와 그 규약. 구현은 `:core:data`가 갖는다([core/data README](../../../../core/data/README.md)). 서버 스키마 원문은 [place-api.md](./place-api.md)·[comment-api.md](./comment-api.md)가 소유한다.

**공통 규약** — 기존 `RoomRepository`와 같다.

- 1회성 요청이므로 `Flow`를 흘리지 않는다.
- 실패를 `Result`로 감싸지 않고 `MinoDomainException`으로 던진다. 취소는 그대로 전파한다([error_handling.md](../../../conventions/error_handling.md)).
- 정렬·필터 책임을 갖지 않는다. 서버가 준 순서를 그대로 돌려준다.

---

## 1. `PlaceRepository` — 신규

```kotlin
interface PlaceRepository {
    suspend fun getPlaceDetail(pinId: String): PlaceDetail
    suspend fun recordAccess(pinId: String)
    suspend fun duplicatePin(pinId: String, roomIds: List<String>)
}
```

### `getPlaceDetail(pinId)`

`GET /api/v1/pins/{pinId}`를 호출해 [data-model.md §1](../data-model.md)의 `PlaceDetail`을 돌려준다.

- **`label`은 서버에서 오지 않는다.** Mapper가 `PlaceLabel.WORTH_VISITING`을 채운다([research.md D12](../research.md)). 서버가 `labelGroup`을 추가하면 이 Mapper 한 곳만 고친다.
- **`roomColor`도 이 응답에 없다.** `roomId`만 오므로 방 목록에서 찾아 채운다 — 채우는 주체는 이 Repository가 아니라 화면이다(§3).
- `createdBy`가 `null`이면 `registrant`도 `null`이다. 기본 아바타 판정은 화면이 한다(EC-004).

### `recordAccess(pinId)`

`POST /api/v1/pins/{pinId}/accesses`를 호출한다(FR-026).

- **반환값이 없다.** 서버의 `{ ok: true }`를 쓰지 않는다.
- **예외를 던지지 않는다.** 이 함수는 `MinoDomainException`을 밖으로 내보내지 않고 구현 안에서 삼킨다 — [spec.md EC-022](../spec.md)가 "화면 동작에 영향을 주지 않는다"를 규정했고, 호출자가 `try`로 감싸야 한다면 그 규칙이 호출부마다 새어 나간다.
- 취소는 예외다. `CancellationException`은 그대로 전파한다.
- 디바운스·중복 제거를 하지 않는다(EC-023). append-only 로그라 서버도 중복을 문제 삼지 않는다.

### `duplicatePin(pinId, roomIds)`

`POST /api/v1/pins/{pinId}/duplicate`를 호출한다(FR-018).

- `roomIds`가 비어 있으면 호출하지 않는다 — 서버 스키마가 `minItems: 1`이다. 빈 목록을 막는 것은 화면의 [공유하기] 비활성 규칙(FR-022)이고, 이 계약은 그 전제를 신뢰한다.
- 반환값이 없다. 성공하면 화면이 토스트를 띄우고 그대로 남는다(FR-018).
- `409`(대상 방 중 하나라도 이미 저장됨)는 `MinoDomainException`으로 전파한다. 별도 분기를 두지 않는다([research.md D14](../research.md)).

## 2. `PlaceCommentRepository` — 신규

```kotlin
interface PlaceCommentRepository {
    suspend fun getComments(pinId: String, page: Int): PlaceCommentPage
    suspend fun addComment(pinId: String, content: String): PlaceComment
    suspend fun deleteComment(pinId: String, commentId: String)
}
```

### `getComments(pinId, page)`

`GET /api/v1/pins/{pinId}/comments?page={page}`를 호출한다.

- **`page 0`이 최신 페이지다.** 목록의 순서를 뒤집지 않는다 — 페이지 안은 이미 오래된 것이 먼저이고, 페이지 사이의 배치는 화면이 정한다([research.md D11](../research.md)).
- `pageSize`를 지정하지 않고 서버 기본값(`example: 20`)을 쓴다. 상한은 100이다.
- `hasNext`를 `PlaceCommentPage.hasOlder`로 옮긴다.

### `addComment(pinId, content)`

`POST /api/v1/pins/{pinId}/comments`를 호출하고 **생성된 코멘트를 돌려준다**(FR-014).

- `content`를 이 계약이 다듬지 않는다. 앞뒤 공백 제거는 서버가 하고, 200자 상한은 입력 단계에서 이미 막힌다(FR-012).
- 목록을 다시 조회하지 않는다. 반환된 항목을 화면이 맨 아래에 덧붙인다.

### `deleteComment(pinId, commentId)`

`DELETE /api/v1/pins/{pinId}/comments/{commentId}`를 호출한다(FR-015).

- 반환값이 없다. 되돌리기 수단을 두지 않으므로 삭제된 항목을 돌려줄 이유가 없다(EC-013).
- 권한 판정을 하지 않는다. 호출 자체가 `canDelete == true`인 코멘트에서만 일어난다([research.md D6](../research.md)).

## 3. `RoomRepository` — **기존 인터페이스 수정**

```kotlin
// 변경 전
suspend fun getRooms(): List<RoomSummary>

// 변경 후
suspend fun getRooms(placeId: String? = null): List<RoomSummary>
```

- `placeId`가 `null`이면 지금과 똑같이 `GET /api/v1/rooms`를 호출한다. 기존 호출자(`GetRoomPickerRoomsUseCase`, `:feature:sharereceiver`)는 고치지 않는다.
- `placeId`가 있으면 `?showHasPlaceId={placeId}`를 붙이고, 응답의 `hasPlace`를 `RoomSummary.hasPlace`에 싣는다([research.md D9](../research.md)).
- `placeId`가 `null`일 때 `RoomSummary.hasPlace`도 `null`이다 — "저장돼 있지 않음"이 아니라 **"판정하지 않음"**이다.

이 확장이 [다른방에 공유] 시트(FR-018·FR-022)와, 이번 범위에서 보류한 [저장된 방] 시트(FR-024)의 목록 원천을 함께 덮는다.

## 4. UseCase를 두지 않는다

이번 feature는 `:core:domain`에 UseCase를 추가하지 않는다.

[core/domain README](../../../../core/domain/README.md)의 기준대로, UseCase는 **여러 Repository를 조합하거나 화면이 소유하면 안 되는 판정**이 있을 때 만든다. 이 화면의 동작은 Repository 호출 하나에 1:1로 대응하고, 유일한 조합(핀 상세의 `roomId`로 방 목록에서 색을 찾는 것)은 화면 표시용 판정이라 ViewModel이 갖는 편이 맞다.

`GetRoomPickerRoomsUseCase`(개인방 최상단 정렬)는 [다른방에 공유] 시트에서도 같은 정렬이 필요하므로 **그대로 재사용한다.** 이 UseCase가 인자 없는 `getRooms()`를 호출하므로 `hasPlace`를 받지 못하는데, **UI 라운드에서는 그것으로 충분하다** — 색상과 목록만 필요하고 이미 저장된 방 표시는 Phase 10의 몫이다([research.md D15](../research.md)). Phase 10에서 `placeId`를 받도록 UseCase 시그니처를 넓힐지 시트가 Repository를 직접 부를지는 그때 정한다 — 판단 근거가 이 spec 밖(공유 시트를 쓰는 다른 화면들)에 있다.
