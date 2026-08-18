# 계약: `RoomRepository` (`:core:domain`)

**대상 plan**: [../plan.md](../plan.md) · 근거: [../research.md](../research.md) D3

이 저장소에는 아직 실제 백엔드 API 문서가 없다(코드베이스 전수 조사 결과 네트워크 계층은 템플릿 예시(`Github*`)뿐). 따라서 이 계약은 REST 엔드포인트가 아니라 **`:feature:room`이 소비하는 Repository 인터페이스**를 UI 계약으로 삼는다. 구현(`RoomRepositoryImpl`)은 `:core:data`가 갖는다(`docs/architecture/modularization.md` 레이어 흐름).

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
