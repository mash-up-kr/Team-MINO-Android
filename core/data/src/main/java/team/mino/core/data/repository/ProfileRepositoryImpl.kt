package team.mino.core.data.repository

import kotlinx.coroutines.flow.Flow
import team.mino.core.data.datasource.ProfileLocalDataSource
import team.mino.core.domain.model.Profile
import team.mino.core.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * 이번 범위의 프로필은 원격이 없어 DTO도 매퍼도 없다 — 로컬 DataSource 위임이 전부다
 * (`docs/specs/profile/contracts/profile-repository-contract.md` §저장 계층).
 *
 * 예외를 잡지 않는다. 저장 실패는 그대로 전파되고 소비는 ViewModel의 `runCatchingDomain`이 한다.
 */
internal class ProfileRepositoryImpl @Inject constructor(
    private val localDataSource: ProfileLocalDataSource,
) : ProfileRepository {
    override fun observeProfile(): Flow<Profile?> = localDataSource.observeProfile()

    override suspend fun saveProfile(profile: Profile) = localDataSource.saveProfile(profile)
}
