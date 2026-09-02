# 계약: 도메인 Repository (Place · PlaceComment · Room 델타)

**대상 스펙 경로**: `docs/specs/place-detail`

**계획서**: [plan.md](../plan.md)

`:core:domain`이 소유하는 인터페이스. 구현은 이번 개정에서 feature 안 Fake에서 `:core:data`로 옮겨간다([research.md D23](../research.md)) — **시그니처는 바뀌지 않으므로 ViewModel과 화면은 손대지 않는다.**

세 인터페이스 모두 1회성 요청이라 `Flow`를 흘리지 않고, 실패를 `Result`로 감싸지 않고 `MinoDomainException`으로 던진다. 취소는 그대로 전파한다. 정렬·필터 책임을 갖지 않는다.

---

## 1. `PlaceRepository`

```kotlin
interface PlaceRepository {
    suspend fun getPlaceDetail(pinId: String): PlaceDetail
    suspend fun recordAccess(pinId: String)
    suspend fun duplicatePin(pinId: String, roomIds: List<String>)
}
```

시그니처는 plan 1.1.0과 같다. KDoc에서 **두 문단이 사실이 아니게 되어 지운다.**

| 지우는 문장 | 사유 |
|---|---|
| "`PlaceDetail.label`을 서버가 주지 않는다 … 서버가 `labelGroup`을 추가하면 Mapper 한 곳만 고친다" | 필드 자체가 삭제됐다([research.md D21](../research.md)) |
| "방 대표 색을 채우지 않는다 … 방 목록에서 찾아 드는 것은 화면의 몫이다" | 마커 색은 `MapPinUiModel`이 이미 든다([place-detail-main-contract.md §1](./place-detail-main-contract.md)) |

남는 규정은 그대로다.

- **`getPlaceDetail`**: 등록자가 없으면 `registrant`가 `null`이고 기본 아바타 대체는 화면이 판정한다(EC-004). **`roomId`를 함께 돌려준다** — 탭 간 진입이 방을 해석하는 근거다([place-detail-entry.md §3.4](./place-detail-entry.md)).
- **`recordAccess`**: `MinoDomainException`을 밖으로 내보내지 않는다. 실패를 구현 안에서 삼키고 재시도하지 않으며(EC-022), 호출자는 결과를 확인하지 않아도 된다. `CancellationException`은 삼키지 않고 전파한다. 디바운스·중복 제거를 하지 않는다(EC-023).
- **`duplicatePin`**: `roomIds`가 비면 호출하지 않는다(서버 `minItems: 1`). 빈 목록을 막는 것은 화면의 [공유하기] 비활성 규칙(FR-022)이고 이 계약은 그 전제를 신뢰한다. 대상 방 중 하나라도 이미 저장돼 있어 서버가 `409`를 주면 그대로 전파한다([research.md D14](../research.md)).

---

## 2. `PlaceCommentRepository`

```kotlin
interface PlaceCommentRepository {
    suspend fun getComments(pinId: String, page: Int): PlaceCommentPage
    suspend fun addComment(pinId: String, content: String): PlaceComment
    suspend fun deleteComment(pinId: String, commentId: String)
}
```

시그니처는 plan 1.1.0과 같다. **반환 타입 `PlaceComment`의 내용이 늘었다** — `createdAt`이 더해졌다([data-model.md §2](../data-model.md)).

- **`getComments`**: `page` 0이 최신 페이지다. 역방향 페이징이라 번호가 커질수록 오래된 코멘트가 온다. 페이지 안은 오래된 것이 먼저이며 **이 계약은 그 순서를 뒤집지 않는다** — 페이지 사이의 배치는 화면이 정한다([research.md D11](../research.md)). `pageSize`를 지정하지 않고 서버 기본값(20)을 쓴다.
- **`addComment`**: 만들어진 코멘트를 돌려준다(FR-014) — 목록을 다시 조회하지 않기 위해서다. 돌려받은 항목의 `createdAt`이 곧 `방금`으로 표기된다(TS-054). `content`를 다듬지 않는다 — 앞뒤 공백 제거는 서버가 하고 200자 상한은 입력 단계에서 막힌다(FR-012).
- **`deleteComment`**: 반환값이 없다(EC-013). 권한을 판정하지 않는다 — 호출 자체가 `canDelete == true`인 코멘트에서만 일어난다([research.md D6](../research.md)).

---

## 3. `RoomRepository` 델타

기존 인터페이스에 **인자 하나가 는다.**

```kotlin
// 변경 전
suspend fun getRooms(): List<RoomSummary>

// 변경 후
suspend fun getRooms(placeId: String? = null): List<RoomSummary>
```

- **기본 인자라 기존 호출자가 깨지지 않는다.** 방 리스트 탭과 기존 공유 시트는 `getRooms()` 그대로다.
- `placeId`를 주면 각 `RoomSummary`의 `hasPlace`·`matchedPinId`가 채워진다. 주지 않으면 둘 다 `null`이다 — "물어보지 않았다"와 "저장돼 있지 않다"를 가르는 구분이다([data-model.md §3](../data-model.md)).
- **정렬 책임은 여전히 없다.** 개인방을 최상단에 고정하는 판정은 `GetRoomPickerRoomsUseCase`가 계속 갖는다.
- 실패는 던진다. 빈 목록으로 수렴시키는 것은 화면의 몫이다.

**한 번의 호출이 세 곳을 먹인다** — 공유 시트(FR-018)·[저장된 방] 버튼 활성(FR-023)·[저장된 방] 시트(FR-024). 상세는 [place-detail-main-contract.md §3.1](./place-detail-main-contract.md).

---

## 4. 구현 배치

| 인터페이스 | 구현 | 바인딩 소유 |
|---|---|---|
| `PlaceRepository` | `:core:data` `PlaceRepositoryImpl` | `:core:data`([ADR 2026-08-02](../../../adr/2026-08-02-di-binding-ownership.md)) |
| `PlaceCommentRepository` | `:core:data` `PlaceCommentRepositoryImpl` | 같음 |
| `RoomRepository` | `:core:data` `RoomRepositoryImpl` (기존 수정) | 기존 |

`:feature:placedetail`의 `fake/`와 `PlaceDetailFakeDataModule`은 삭제된다 — 모듈 자체가 사라진다.
