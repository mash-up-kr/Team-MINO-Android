package team.mino.core.data.datasource

import team.mino.core.data.network.dto.response.PinResponse

/**
 * 장소(핀)의 원격 출처. [getPins]·[duplicatePin]의 계약은
 * `docs/specs/room-detail/contracts/place-repository.md` 근거.
 *
 * `deletePlace`에 대응하는 서버 엔드포인트가 아직 없어([docs/specs/room-detail/research.md] D14) 이
 * DataSource에는 삭제 메서드를 두지 않는다 — 임시 처리는 `RoomPlacesRepositoryImpl`이 갖는다.
 */
internal interface PlaceRemoteDataSource {
    /** 특정 방에 저장된 핀 전체 조회. `GET /api/v1/pins?roomId={roomId}`. */
    suspend fun getPins(roomId: String): List<PinResponse>

    /**
     * 핀을 다른 방들에 복제. `POST /api/v1/pins/{pinId}/duplicate`.
     * 대상 방 중 하나라도 같은 장소가 이미 있으면 서버가 `409 DUPLICATE_PIN_IN_ROOM`으로 전체 거절한다.
     */
    suspend fun duplicatePin(
        pinId: String,
        targetRoomIds: List<String>,
    )
}
