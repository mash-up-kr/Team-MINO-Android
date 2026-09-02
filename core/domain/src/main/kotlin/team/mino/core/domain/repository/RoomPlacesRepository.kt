package team.mino.core.domain.repository

import kotlinx.coroutines.flow.Flow
import team.mino.core.domain.model.Place

/**
 * 방에 저장된 장소(핀) 목록 단위 동작 계약 — 핀 한 개 자체의 조회·기록·복제는 [PlaceRepository] 소관이다.
 *
 * `observePlaces`는 배포된 서버 API로 근거를 대조했다(`docs/specs/room-detail/research.md` D14).
 * `deletePlace`는 대응 엔드포인트가 아직 없어 구현 시점 임시 처리로 남는다.
 *
 * **다른 방에 공유는 여기 없다.** room-detail(#154)과 place-detail(#161)이 같은 엔드포인트
 * (`POST /pins/{pinId}/duplicate`)에 대고 각자 `sharePlaces`와 [PlaceRepository.duplicatePin]을 만들었고,
 * 두 화면이 [SYS-003] 시트를 한 벌로 합치면서 [PlaceRepository] 쪽으로 모았다.
 */
interface RoomPlacesRepository {
    /** 특정 방에 저장된 장소(핀) 전체를 실시간 관찰. */
    fun observePlaces(roomId: String): Flow<List<Place>>

    /**
     * [FR-010] 장소 삭제 — 호출한 방에서만 제거한다(다른 방에 복제된 사본은 남는다).
     *
     * 서버 계약 `[TBD]`(`docs/specs/room-detail/research.md` D14) — 대응 엔드포인트가 아직 없어 구현은
     * 임시 처리로 남는다.
     */
    suspend fun deletePlace(
        roomId: String,
        pinId: String,
    )
}
