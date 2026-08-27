# 계약: `PlaceRepository` (`:core:domain`)

**대상 plan**: [../plan.md](../plan.md) · 근거: [../research.md](../research.md) D7·D8

백엔드 API는 room-list와 마찬가지로 draft 단계라(근거는 [room-list/research.md D12](../../room-list/research.md)) 이 계약도 REST 엔드포인트가 아니라 **`:feature:room/detail/`이 소비하는 Repository 인터페이스**를 UI 계약으로 삼는다. 구현(`PlaceRepositoryImpl`)은 `:core:data`가 갖는다(`docs/architecture/modularization.md` 레이어 흐름).

```kotlin
package team.mino.core.domain.repository

interface PlaceRepository {
    /** 특정 방에 저장된 장소 전체를 실시간 관찰. */
    fun observePlaces(roomId: String): Flow<List<Place>>

    /** [SYS-003] 다른 방에 공유 — 하나의 장소를 여러 방에 한 번에 복제한다. */
    suspend fun sharePlaces(placeId: String, targetRoomIds: List<String>)

    /** [FR-010] 장소 삭제 — 호출한 방에서만 제거한다(다른 방에 복제된 사본은 남는다). */
    suspend fun deletePlace(roomId: String, placeId: String)
}
```

- `observePlaces(roomId)`가 `Flow`인 이유: 다른 화면(장소 상세·다른 방에 공유)에서 장소가 바뀌면 방 상세가 재구독 없이 최신값을 반영해야 한다(room-list의 `observeMyRooms()`와 같은 논리).
- 정렬·필터([FR-005]·[FR-006])는 이 계약이 아니라 **클라이언트 쪽**에서 `List<Place>`를 가공한다 — room-list의 `RoomRepository`와 동일한 이유(서버 정렬 API 없음, [room-list/contracts/room-repository.md](../../room-list/contracts/room-repository.md)).
- `sharePlaces`의 서버 요청 스키마(한 번에 여러 방을 어떻게 표현하는지, 이미 저장된 방을 서버가 어떻게 무시/거부하는지)는 **[TBD]** — [SYS-003] 전용 spec이 이 저장소에 아직 없다([research.md D10](../research.md)). 시그니처는 spec.md FR-009("체크박스 복수 선택 → [공유하기]")가 요구하는 입력 형태(장소 1개 + 방 여러 개)만 확정했다.
- `deletePlace`가 `roomId`를 받는 이유: [FR-010]이 "해당 방에서만 장소를 제거"한다고 명시해([spec.md 유저 플로우 3-3](../spec.md)) 장소가 여러 방에 복제돼 있을 수 있다는 전제를 반영했다.

## 구현 위치

- **구현**: `core:data/repository/PlaceRepositoryImpl` — `datasource/PlaceRemoteDataSource`(Ktor) 하나만 호출. DataSource는 DTO를 반환하고 `RepositoryImpl`이 `repository/mapper/PlaceMapper.kt`로 `Place`로 변환한다(`core:data/README.md` §6·§7, room-list `RoomRepositoryImpl`과 동일 패턴).
- **DI**: `core:data/repository/di/PlaceRepositoryModule.kt` — `@Binds @Singleton`.
- **DTO 갭 대응**: draft API가 이 저장소의 방 목록 엔드포인트에서 장소 상세 필드를 아직 노출하지 않는다(room-list D12가 이미 확인한 draft 문서 범위 밖). `sharePlaces`·`deletePlace`의 서버 응답 형태를 포함해 구현 시점의 임시 목데이터/플레이스홀더로 채운다.

## 소비자

| Repository | 소비 화면 | 소비 방식 |
|---|---|---|
| `PlaceRepository` | `RoomDetailMain`([contracts/room-detail-main-contract.md](./room-detail-main-contract.md)) | ViewModel 직접 호출(UseCase 없음) — 정렬·필터링은 화면이 `List<Place>`를 가공하는 표현 로직이라 UseCase로 올리지 않는다(`core:domain/README.md` §4, room-list와 동일 근거) |
