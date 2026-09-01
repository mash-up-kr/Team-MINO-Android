package team.mino.core.domain.repository

import kotlinx.coroutines.flow.Flow
import team.mino.core.domain.model.Place

/**
 * 방에 저장된 장소(핀) 목록 단위 동작 계약 — 핀 한 개 자체의 조회·기록은 [PlaceRepository] 소관이다.
 *
 * `observePlaces`는 배포된 서버 API로 근거를 대조했다(`docs/specs/room-detail/research.md` D14).
 * `deletePlace`는 대응 엔드포인트가 아직 없어 구현 시점 임시 처리로 남는다.
 *
 * **[sharePlaces]는 [PlaceRepository.duplicatePin]과 같은 서버 엔드포인트
 * (`POST /pins/{pinId}/duplicate`)를 가리키는 중복이다.** room-detail(#154)과 place-detail(#161)이
 * 각자 독립적으로 만들어 develop 병합 시점(2026-08-30)에 합쳐지지 않았다 — room-detail이 [PlaceRepository]
 * 쪽으로 갈아타면 이 메서드는 지워질 수 있다.
 */
interface RoomPlacesRepository {
    /** 특정 방에 저장된 장소(핀) 전체를 실시간 관찰. */
    fun observePlaces(roomId: String): Flow<List<Place>>

    /**
     * [SYS-003] 다른 방에 공유 — 하나의 핀을 여러 방에 한 번에 복제한다.
     *
     * [pinId]는 [Place.id]([Place] 참고, 실제로는 서버 `Pin.id`)다. 대상 방 중 하나라도 같은 장소가 이미
     * 저장돼 있으면 서버가 `409 DUPLICATE_PIN_IN_ROOM`으로 전체 거절하고, 이는 `MinoDomainException`으로
     * 매핑돼 던져진다.
     */
    suspend fun sharePlaces(
        pinId: String,
        targetRoomIds: List<String>,
    )

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
