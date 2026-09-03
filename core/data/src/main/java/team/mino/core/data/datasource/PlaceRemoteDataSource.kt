package team.mino.core.data.datasource

import team.mino.core.data.network.dto.response.PinResponse

/**
 * 장소(핀)의 원격 출처. [getPins]·[deletePin]의 계약은
 * `docs/specs/room-detail/contracts/place-repository.md` 근거.
 *
 * **복제(다른 방에 공유)는 여기 없다.** 같은 엔드포인트를 `PinRemoteDataSource.duplicatePin`이 이미
 * 가리키고 있어 [team.mino.core.domain.repository.PlaceRepository] 한 갈래로 모았다.
 */
internal interface PlaceRemoteDataSource {
    /** 특정 방에 저장된 핀 전체 조회. `GET /api/v1/pins?roomId={roomId}`. */
    suspend fun getPins(roomId: String): List<PinResponse>

    /** 특정 핀 삭제. `DELETE /api/v1/pins/{pinId}`. */
    suspend fun deletePin(pinId: String)
}
