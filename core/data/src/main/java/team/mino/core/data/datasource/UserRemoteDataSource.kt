package team.mino.core.data.datasource

internal interface UserRemoteDataSource {
    /**
     * `GET /api/v1/users/me`가 프로필 존재를 확인해 주는지. 미등록(`401` + `USER_NOT_REGISTERED`)만 `false`이고
     * 그 밖의 실패는 [team.mino.core.errorhandling.MinoDomainException]으로 던진다.
     */
    suspend fun isRegistered(): Boolean
}
