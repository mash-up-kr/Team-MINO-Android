package team.mino.core.data.datasource

import team.mino.core.data.network.dto.request.ProfileRequest
import team.mino.core.data.network.dto.response.ProfileResponse

internal interface UserRemoteDataSource {
    /**
     * `GET /api/v1/users/me`가 프로필 존재를 확인해 주는지. 미등록(`401` + `USER_NOT_REGISTERED`)만 `false`이고
     * 그 밖의 실패는 [team.mino.core.errorhandling.MinoDomainException]으로 던진다.
     */
    suspend fun isRegistered(): Boolean

    /** 내 프로필. 미등록(`401` + `USER_NOT_REGISTERED`)이면 `null`이다. */
    suspend fun getMe(): ProfileResponse?

    suspend fun register(request: ProfileRequest): ProfileResponse

    suspend fun updateMe(request: ProfileRequest): ProfileResponse

    suspend fun putPushToken(
        token: String,
        platform: String,
    )
}
