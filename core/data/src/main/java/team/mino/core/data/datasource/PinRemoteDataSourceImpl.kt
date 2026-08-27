package team.mino.core.data.datasource

import team.mino.core.data.network.dto.request.PinCreateRequest
import team.mino.core.data.network.service.PinApiService
import javax.inject.Inject

internal class PinRemoteDataSourceImpl @Inject constructor(
    private val service: PinApiService,
) : PinRemoteDataSource {
    override suspend fun createPin(request: PinCreateRequest) = service.createPin(request)
}
