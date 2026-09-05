# 계약: `PlaceRepository` (`:core:domain`)

**대상 plan**: [../plan.md](../plan.md) · 근거: [../research.md](../research.md) D7·D8·D14

`observePlaces`·`sharePlaces`는 배포된 서버 API(`https://api.gguk.org`, `GET /api-docs-json`, 조회 2026-08-27T20:54:28+09:00, [research.md D14](../research.md))로 근거를 대조했다. `deletePlace`는 이 문서 작성 시점에 대응 엔드포인트가 없어 클라이언트 임시 처리로 남겼으나, **2026-09-03 백엔드가 `DELETE /api/v1/pins/{pinId}`를 배포하면서 해소됐다**(아래 갱신 항목 참조).

```kotlin
package team.mino.core.domain.repository

interface PlaceRepository {
    /** 특정 방에 저장된 장소(핀) 전체를 실시간 관찰. */
    fun observePlaces(roomId: String): Flow<List<Place>>

    /** [SYS-003] 다른 방에 공유 — 하나의 핀을 여러 방에 한 번에 복제한다. */
    suspend fun sharePlaces(pinId: String, targetRoomIds: List<String>)

    /** [FR-010] 장소 삭제 — 호출한 방에서만 제거한다(다른 방에 복제된 사본은 남는다). `DELETE /api/v1/pins/{pinId}` */
    suspend fun deletePlace(roomId: String, pinId: String)
}
```

- `observePlaces(roomId)`가 `Flow`인 이유: 다른 화면(장소 상세·다른 방에 공유)에서 장소가 바뀌면 방 상세가 재구독 없이 최신값을 반영해야 한다(room-list의 `observeMyRooms()`와 같은 논리).
- 정렬·필터([FR-005]·[FR-006])는 이 계약이 아니라 **클라이언트 쪽**에서 `List<Place>`를 가공한다 — room-list의 `RoomRepository`와 동일한 이유(서버 정렬 API 없음, [room-list/contracts/room-repository.md](../../room-list/contracts/room-repository.md)).
- **`sharePlaces(pinId, targetRoomIds)`** — `POST /api/v1/pins/{pinId}/duplicate`, body `{ roomIds: string[] (minItems 1) }`. 파라미터 이름이 `placeId`가 아니라 `pinId`인 이유: 엔드포인트 경로 자체가 `/pins/{pinId}/duplicate`이고, `Place.id`([data-model.md §1](../data-model.md))도 실제로는 서버 `Pin.id`를 담고 있다. 대상 방 중 하나라도 같은 장소가 이미 저장돼 있으면 서버가 `409 DUPLICATE_PIN_IN_ROOM`으로 전체 거절한다 — 클라이언트는 이 응답을 `MinoDomainException`으로 매핑해 위로 던진다(`docs/conventions/error_handling.md`).
- **`deletePlace`가 `roomId`를 받는 이유**: [FR-010]이 "해당 방에서만 장소를 제거"한다고 명시해([spec.md 유저 플로우 3-3](../spec.md)) 장소가 여러 방에 복제돼 있을 수 있다는 전제를 반영했다. **(2026-09-03 재검증 완료)** 배포된 엔드포인트는 `DELETE /api/v1/pins/{pinId}` 단건이라 `roomId`를 받지 않는다. 그럼에도 이 시그니처를 유지한다 — 핀 레코드(`PinResponse.roomId`)가 방 하나에 1:1로 귀속되고 다른 방 복제는 새 `pinId`를 발급하므로 단건 삭제가 곧 "그 방에서만 제거"이고, `roomId`는 호출자의 의도를 도메인 계약에 남기는 값으로 남는다. 요청에 싣지 않는 판단은 `RoomPlacesRepositoryImpl`이 흡수한다.

## 구현 위치

- **구현**: `core:data/repository/PlaceRepositoryImpl` — `datasource/PlaceRemoteDataSource`(Ktor) 하나만 호출. DataSource는 DTO(`PinResponse`)를 반환하고 `RepositoryImpl`이 `repository/mapper/PlaceMapper.kt`로 `Place`로 변환한다(`core:data/README.md` §6·§7, room-list `RoomRepositoryImpl`과 동일 패턴). `PinResponse`는 `id·roomId·place: PlaceResponse·images: List<String>·createdBy·createdAt`을 그대로 반영하고, `PlaceResponse`는 서버 `Pin.place` 스키마(`id·provider·providerPlaceId·name·address·city·district·lat·lng·category·phone·mapUrl·createdAt·updatedAt`)를 그대로 반영한다([research.md D14](../research.md)).
- **DI**: `core:data/repository/di/PlaceRepositoryModule.kt` — `@Binds @Singleton`.
- **DTO 갭 대응**: `commentCount`·`isGgukPick`([data-model.md §4](../data-model.md))은 서버 응답에 없어 Mapper 단계에서 임시 목데이터/플레이스홀더로 채운다. 백엔드가 필드를 확정하면 Mapper·DataSource만 교체하면 되도록, `PlaceRepository` 인터페이스와 [data-model.md](../data-model.md)의 `Place`는 이 갭 때문에 바꾸지 않는다.
- **(2026-09-03 갱신) `deletePlace` 갭 해소**: 백엔드가 `DELETE /api/v1/pins/{pinId}`를 배포해 구현 시점 임시 처리(`no-op`)를 걷어냈다. 인터페이스는 위 설계대로 바뀌지 않았고 `PlaceRemoteDataSource.deletePin`·`PlaceApiService.deletePin`만 늘었다 — 갭을 인터페이스 밖에 가둬 둔 판단이 실제로 값을 했다.

## 소비자

| Repository | 소비 화면 | 소비 방식 |
|---|---|---|
| `PlaceRepository` | `RoomDetailMain`([contracts/room-detail-main-contract.md](./room-detail-main-contract.md)) | ViewModel 직접 호출(UseCase 없음) — 정렬·필터링은 화면이 `List<Place>`를 가공하는 표현 로직이라 UseCase로 올리지 않는다(`core:domain/README.md` §4, room-list와 동일 근거) |

---

## `RoomRepository` 확장 ([research.md D15·D16](../research.md))

room-list가 정의한 `RoomRepository`(`:core:domain`, [room-list/contracts/room-repository.md](../../room-list/contracts/room-repository.md))에 이 spec이 필요로 하는 방 단위 동작 4개를 추가한다. **이 문서는 추가할 메서드만 선언한다 — `RoomRepository`의 공식 계약 문서는 room-list 쪽이 소유하므로, 그 문서 자체의 갱신은 이 plan의 범위 밖이다(완료 보고 참고).**

```kotlin
package team.mino.core.domain.repository

interface RoomRepository {
    // ... 기존 observeMyRooms · getRoom · createRoom · updateRoom (room-list 정의, 변경 없음)

    /** [FR-011]·[FR-013] 방 멤버 전체 목록 — 초대 시트 참여자 목록과 방장 위임 대상 선택이 공유. */
    suspend fun getMembers(roomId: String): List<RoomMember>

    /** [FR-011] 내 초대 링크 발급 — 이미 발급했다면 서버가 같은 code를 돌려준다. */
    suspend fun createInvitation(roomId: String): String

    /** [FR-013] 방 나가기 — 방장이 위임 없이 호출하면 OWNER_TRANSFER_REQUIRED 도메인 예외. */
    suspend fun leaveRoom(roomId: String)

    /** [FR-013] 방장 위임 — 위임 대상 선택 후 호출, 성공하면 leaveRoom을 이어서 호출한다. */
    suspend fun transferOwner(roomId: String, nextOwnerId: String)
}
```

- **`getMembers(roomId)`** — `GET /api/v1/rooms/{roomId}/members`, 응답 `{userId, nickname, avatar, isOwner, joinedAt}[]`을 `RoomMember`([data-model.md §1](../data-model.md))로 매핑.
- **`createInvitation(roomId)`** — `POST /api/v1/rooms/{roomId}/invitations`, 응답 `{ code }`(6자 대문자+숫자). 클라이언트가 `gguk.org/r/{code}`로 링크를 조립한다(서버가 완성된 URL을 주지 않는다). 개인방 호출 시 서버가 `403 PERSONAL_ROOM_NOT_ALLOWED`.
- **`leaveRoom(roomId)`** — `DELETE /api/v1/rooms/{roomId}/members/me`. 방장이 다른 멤버가 있는 채로 호출하면 서버가 `409 OWNER_TRANSFER_REQUIRED`를 낸다 — `RoomDetailViewModel`은 이 도메인 예외를 잡아 `leaveDialogState`를 `DelegateOwner`로 전이한다([contracts/room-detail-main-contract.md](./room-detail-main-contract.md) "나가기 플로우" 분기 규칙, 별도 사전 멤버 수 조회 불필요). 방장이 마지막 1인이면 서버가 방을 자동 삭제한다.
- **`transferOwner(roomId, nextOwnerId)`** — `PUT /api/v1/rooms/{roomId}/owner`, body `{ nextOwnerId }`. 성공(`200`) 후 `leaveRoom(roomId)`을 이어서 호출해 나가기를 완료한다([contracts/room-detail-main-contract.md](./room-detail-main-contract.md) `OnOwnerDelegateConfirm` 처리).

### 구현 위치

- **구현**: `core:data/repository/RoomRepositoryImpl`(room-list가 이미 만든 클래스)에 위 4개 메서드를 추가한다. `RoomRemoteDataSource`(Ktor)에 대응 호출을 더하고, 에러 코드(`OWNER_TRANSFER_REQUIRED`·`PERSONAL_ROOM_NOT_ALLOWED`·`NOT_ROOM_MEMBER`)는 `core:error-handling`의 `runCatchingDomain`으로 `MinoDomainException` 리프에 매핑한다(`docs/conventions/error_handling.md`).
- **소비**: `RoomDetailViewModel`(`RoomInviteSheet`·`RoomLeaveConfirmDialog`·`RoomOwnerLeaveDialog`가 트리거)이 직접 호출.

### 소비자

| Repository 확장 | 소비 화면 | 소비 방식 |
|---|---|---|
| `RoomRepository.getMembers`·`createInvitation`·`leaveRoom`·`transferOwner` | `RoomDetailMain` | ViewModel 직접 호출(UseCase 없음) |
