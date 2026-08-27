# 계약: `RoomRepository` (`:core:domain`)

**대상 plan**: [../plan.md](../plan.md) · 근거: [../research.md](../research.md) D3·D15

백엔드 API는 아직 draft 단계라(코드베이스 네트워크 계층도 템플릿 예시(`Github*`)뿐 — 조사 근거는 [research.md D12](../research.md)) 이 계약은 REST 엔드포인트가 아니라 **`:feature:room`이 소비하는 Repository 인터페이스**를 UI 계약으로 삼는다. 구현(`RoomRepositoryImpl`)은 `:core:data`가 갖는다(`docs/architecture/modularization.md` 레이어 흐름).

```kotlin
package team.mino.core.domain.repository

interface RoomRepository {
    /** 내가 속한 모든 방(개인방 + 공동방)을 실시간 관찰. 개인방은 항상 포함된다. */
    fun observeMyRooms(): Flow<List<Room>>

    /** 단건 조회 — 방 상세 진입 시 캐시 미스 등 필요할 때만. 목록 화면은 observeMyRooms로 충분. */
    suspend fun getRoom(roomId: String): Room

    /** 방을 만들고 만들어진 방을 돌려준다. */
    suspend fun createRoom(draft: RoomDraft): Room

    /** 방의 내용을 draft로 바꾸고 수정된 방을 돌려준다. */
    suspend fun updateRoom(roomId: String, draft: RoomDraft): Room

    /** [room-detail SYS-006·SYS-007] 방 멤버 전체 목록 — 초대 참여자 목록 + 위임 대상 선택 공용. */
    suspend fun getMembers(roomId: String): List<RoomMember>

    /** [room-detail SYS-006] 내 초대 링크 발급 — 이미 발급했다면 서버가 같은 code를 돌려준다. */
    suspend fun createInvitation(roomId: String): String

    /** [room-detail SYS-007] 방 나가기 — 방장이 위임 없이 호출하면 OWNER_TRANSFER_REQUIRED 도메인 예외. */
    suspend fun leaveRoom(roomId: String)

    /** [room-detail SYS-007] 방장 위임. */
    suspend fun transferOwner(roomId: String, nextOwnerId: String)
}
```

- `observeMyRooms()`가 `Flow`인 이유: 다른 화면(방 생성 폼·방 상세)에서 방 정보가 바뀌면 방 리스트 탭이 재구독 없이 최신값을 반영해야 한다([spec.md 유저 플로우 3](../spec.md) — 방 생성 후 방 리스트에도 반영).
- 정렬·필터([FR-005]·[FR-011])는 이 계약이 아니라 **클라이언트 쪽**에서 `List<Room>`을 가공한다 — 서버 정렬 API가 없다는 전제(백엔드 계약 미문서화, 위 참고). 서버 정렬이 실제로 필요해지면(성능·페이지네이션 이슈) 이 계약을 개정한다.
- 카테고리 필터([FR-011])는 `Room`이 아니라 지도 마커(장소 단위) 대상이라 이 계약 밖이다 — `place-repository`(다른 spec 소유, [spec.md §3.2](../spec.md) 참고)가 담당할 영역.
- **`getMembers`·`createInvitation`·`leaveRoom`·`transferOwner`**([research.md D15](../research.md))는 room-list가 아니라 room-detail의 [SYS-006]·[SYS-007]이 쓰는 동작이지만, API tag가 `room`인 방 단위 동작이라 이 Repository에 둔다. 시그니처 근거·서버 엔드포인트 대조는 room-detail이 소유한 [room-detail/contracts/place-repository.md](../../room-detail/contracts/place-repository.md) "`RoomRepository` 확장" 절 참조 — 이 문서는 인터페이스 전체 모습만 최신으로 유지하고 상세 근거는 중복 기술하지 않는다.
- **구현 상태**: 이 네 메서드는 이 문서(계약)에는 반영됐지만 `:core:domain`의 실제 `RoomRepository.kt`·`RoomRepositoryImpl`·테스트 더블에는 아직 반영되지 않았다(room-detail의 `/mino-task`가 담당). room-list 쪽 기존 구현·테스트는 새 메서드를 호출하지 않으므로 이 문서 갱신 자체는 기존 코드에 영향이 없다.

## 구현 위치

- **구현**: `core:data/repository/RoomRepositoryImpl` — `datasource/RoomRemoteDataSource`(Ktor) 하나만 호출. DataSource는 DTO(`RoomSummaryResponse` 등)를 반환하고, `RepositoryImpl`이 `repository/mapper/RoomMapper.kt`(`RoomSummaryResponse.toDomain()`)로 `Room`으로 변환한다(`core:data/README.md` §6·§7).
- **DI**: `core:data/repository/di/RoomRepositoryModule.kt` — `@Binds @Singleton`.
- **DTO 갭 대응**: draft API가 아직 제공하지 않는 필드(`RoomThumbnail`·`RoomMemberSummary.visibleAvatarUrls`·`lastPlaceSavedAt`·`commentCount` — [research.md D12](../research.md))는 Mapper 단계에서 임시 목데이터/플레이스홀더로 채운다. 백엔드가 필드를 확정하면 Mapper만 교체하면 되도록, `RoomRepository` 인터페이스와 [data-model.md](../data-model.md)의 `Room`은 draft API 형태를 따라 바꾸지 않는다.

## 소비자

| Repository | 소비 화면 | 소비 방식 |
|---|---|---|
| `RoomRepository` | `RoomListMain`([contracts/room-list-main-contract.md](./room-list-main-contract.md)) | ViewModel 직접 호출(UseCase 없음) — 정렬·필터링은 화면이 `List<Room>`을 가공하는 표현 로직이라 UseCase로 올리지 않는다(`core:domain/README.md` §4) |
| `RoomRepository.getMembers`·`createInvitation`·`leaveRoom`·`transferOwner` | `RoomDetailMain`([room-detail/contracts/room-detail-main-contract.md](../../room-detail/contracts/room-detail-main-contract.md)) | ViewModel 직접 호출(UseCase 없음), [research.md D15](../research.md) |
