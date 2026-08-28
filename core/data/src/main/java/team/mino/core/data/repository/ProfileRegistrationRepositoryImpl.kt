package team.mino.core.data.repository

import team.mino.core.data.datasource.UserRemoteDataSource
import team.mino.core.domain.repository.ProfileRegistrationRepository
import javax.inject.Inject

internal class ProfileRegistrationRepositoryImpl @Inject constructor(
    private val userRemoteDataSource: UserRemoteDataSource,
) : ProfileRegistrationRepository {
    override suspend fun isRegistered(): Boolean = userRemoteDataSource.isRegistered()
}
