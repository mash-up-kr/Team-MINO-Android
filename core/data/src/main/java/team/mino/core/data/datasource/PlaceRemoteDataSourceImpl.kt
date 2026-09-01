package team.mino.core.data.datasource

import team.mino.core.data.network.dto.response.PinResponse
import team.mino.core.data.network.service.PlaceApiService
import javax.inject.Inject

/**
 * [PlaceRemoteDataSource]의 유일한 구현. `PlaceApiService`(Ktor) 호출을 그대로 위임한다 — 변환·비즈니스
 * 로직은 두지 않는다(`core/data/README.md` §5).
 */
internal class PlaceRemoteDataSourceImpl @Inject constructor(
    private val service: PlaceApiService,
) : PlaceRemoteDataSource {
    override suspend fun getPins(roomId: String): List<PinResponse> = service.getPins(roomId)

    override suspend fun duplicatePin(
        pinId: String,
        targetRoomIds: List<String>,
    ) {
        service.duplicatePin(pinId, targetRoomIds)
    }
}
