package team.mino.core.data.datasource

import team.mino.core.data.network.dto.request.ProfileRequest
import team.mino.core.data.network.dto.response.ProfileResponse
import team.mino.core.data.network.service.UserApiService
import javax.inject.Inject

/**
 * `user` 태그 출처를 부르기만 한다(`core/data/README.md` §5). `401` 하나가 "미등록"과 "인증 실패" 둘을 겸하는
 * 엔드포인트의 판정은 [UserApiService]가 지역 catch로 끝내므로, Ktor 타입도 `errorCode` 문자열도 이 파일에
 * 나타나지 않는다.
 */
internal class UserRemoteDataSourceImpl @Inject constructor(
    private val service: UserApiService,
) : UserRemoteDataSource {
    override suspend fun isRegistered(): Boolean = service.hasProfile()

    override suspend fun getMe(): ProfileResponse? = service.getMe()

    override suspend fun register(request: ProfileRequest): ProfileResponse = service.register(request)

    override suspend fun updateMe(request: ProfileRequest): ProfileResponse = service.updateMe(request)

    override suspend fun putPushToken(
        token: String,
        platform: String,
    ) = service.putPushToken(token, platform)
}
