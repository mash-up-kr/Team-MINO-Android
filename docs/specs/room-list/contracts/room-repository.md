# 계약: `RoomRepository` (`:core:domain`)

**대상 plan**: [../plan.md](../plan.md) · 근거: [../research.md](../research.md) D3

백엔드 API는 아직 draft 단계라(코드베이스 네트워크 계층도 템플릿 예시(`Github*`)뿐 — 조사 근거는 [research.md D12](../research.md)) 이 계약은 REST 엔드포인트가 아니라 **`:feature:room`이 소비하는 Repository 인터페이스**를 UI 계약으로 삼는다. 구현(`RoomRepositoryImpl`)은 `:core:data`가 갖는다(`docs/architecture/modularization.md` 레이어 흐름).

```kotlin
package team.mino.core.domain.repository

interface RoomRepository {
    /** 내가 속한 모든 방(개인방 + 공동방)을 실시간 관찰. 개인방은 항상 포함된다. */
    fun observeMyRooms(): Flow<List<Room>>

    /** 단건 조회 — 방 상세 진입 시 캐시 미스 등 필요할 때만. 목록 화면은 observeMyRooms로 충분. */
    suspend fun getRoom(roomId: String): Room
}
```

- `observeMyRooms()`가 `Flow`인 이유: 다른 화면(방 생성 폼·방 상세)에서 방 정보가 바뀌면 방 리스트 탭이 재구독 없이 최신값을 반영해야 한다([spec.md 유저 플로우 3](../spec.md) — 방 생성 후 방 리스트에도 반영).
- 정렬·필터([FR-005]·[FR-011])는 이 계약이 아니라 **클라이언트 쪽**에서 `List<Room>`을 가공한다 — 서버 정렬 API가 없다는 전제(백엔드 계약 미문서화, 위 참고). 서버 정렬이 실제로 필요해지면(성능·페이지네이션 이슈) 이 계약을 개정한다.
- 카테고리 필터([FR-011])는 `Room`이 아니라 지도 마커(장소 단위) 대상이라 이 계약 밖이다 — `place-repository`(다른 spec 소유, [spec.md §3.2](../spec.md) 참고)가 담당할 영역.

## 구현 위치

- **구현**: `core:data/repository/RoomRepositoryImpl` — `datasource/RoomRemoteDataSource`(Ktor) 하나만 호출. DataSource는 DTO(`RoomSummaryResponse` 등)를 반환하고, `RepositoryImpl`이 `repository/mapper/RoomMapper.kt`(`RoomSummaryResponse.toDomain()`)로 `Room`으로 변환한다(`core:data/README.md` §6·§7).
- **DI**: `core:data/repository/di/RoomRepositoryModule.kt` — `@Binds @Singleton`.
- **DTO 갭 대응**: draft API가 아직 제공하지 않는 필드(`RoomThumbnail`·`RoomMemberSummary.visibleAvatarUrls`·`lastPlaceSavedAt`·`commentCount` — [research.md D12](../research.md))는 Mapper 단계에서 임시 목데이터/플레이스홀더로 채운다. 백엔드가 필드를 확정하면 Mapper만 교체하면 되도록, `RoomRepository` 인터페이스와 [data-model.md](../data-model.md)의 `Room`은 draft API 형태를 따라 바꾸지 않는다.

## 소비자

| Repository | 소비 화면 | 소비 방식 |
|---|---|---|
| `RoomRepository` | `RoomListMain`([contracts/room-list-main-contract.md](./room-list-main-contract.md)) | ViewModel 직접 호출(UseCase 없음) — 정렬·필터링은 화면이 `List<Room>`을 가공하는 표현 로직이라 UseCase로 올리지 않는다(`core:domain/README.md` §4) |
