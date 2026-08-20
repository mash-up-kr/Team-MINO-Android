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

## 백엔드 참고 (draft) — `:core:data` 구현 시 DTO 참고 자료

`Team-MINO-Node` 저장소의 draft OpenAPI 문서를 이 계약이 확정 근거로 삼지는 않지만(아래 이유), `RoomRepositoryImpl` 작성 시 참고할 DTO 형태로 남긴다. 근거: [research.md D12](../research.md).

- **문서**: [swagger.yaml](https://raw.githubusercontent.com/mash-up-kr/Team-MINO-Node/refs/heads/KKardy/GM-111-outline-prd/docs/swagger.yaml) — `info.version: 0.1.0-draft`, 브랜치 `KKardy/GM-111-outline-prd`(미merge, "outline" 단계). 확정 계약이 아니므로 이 문서의 필드가 바뀌어도 `RoomRepository` 인터페이스나 [data-model.md](../data-model.md)의 `Room`을 그대로 따라 바꾸지 않는다.
- **제공 엔드포인트(참고)**: `GET /api/v1/rooms`(`RoomSummary[]` — `id`·`type`·`name`·`description`·`color`(hex)·`ownerId`·`inviteCode`·`createdAt`·`pinCount`·`memberCount`·옵션 `hasPlace`·옵션 `users: RoomMember[]`), `GET /api/v1/rooms/{roomId}`(`RoomDetail` — `RoomSummary` + `pinCount`·`memberCount`).
- **필드 갭(이 spec이 요구하지만 draft에 없음)**: `RoomThumbnail`(콜라주 이미지 URL), `RoomMemberSummary.visibleAvatarUrls`(draft는 `RoomMember.avatar: { id: integer }`만 제공, URL 매핑 없음), `lastPlaceSavedAt`, `commentCount`, 서버 정렬·필터 쿼리("기획 TBD"로 문서에 명시).
- **구현 방향**: 갭 필드는 `:core:data` 구현 시점에 임시 목데이터/플레이스홀더로 채운다. 이 작업 자체는 이 plan이 아니라 `/mino-task`·구현 단계의 몫이다.
