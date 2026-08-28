package team.mino.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import team.mino.core.data.datasource.ProfileLocalDataSource
import team.mino.core.data.datasource.UserRemoteDataSource
import team.mino.core.data.repository.mapper.toDomain
import team.mino.core.data.repository.mapper.toEntry
import team.mino.core.data.repository.mapper.toRequest
import team.mino.core.domain.model.Profile
import team.mino.core.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * 원격이 원천이고 로컬은 그 응답의 캐시다
 * (`docs/specs/profile/contracts/profile-repository-contract.md` §저장의 불변식 · research.md D36).
 *
 * 변환의 경계가 여기다 — DTO(`ProfileResponse`·`ProfileEntry`)는 이 클래스 밖으로 나가지 않는다
 * (`core/data/README.md` §5·§6 · D42).
 *
 * 예외를 잡지 않는다. 네트워크·HTTP 실패는 `MinoDomainException`으로 그대로 올라가고 소비는 ViewModel의
 * `runCatchingDomain`이 한다(에러 처리 규약 §3).
 */
internal class ProfileRepositoryImpl @Inject constructor(
    private val remoteDataSource: UserRemoteDataSource,
    private val localDataSource: ProfileLocalDataSource,
) : ProfileRepository {
    override fun observeProfile(): Flow<Profile?> = localDataSource.observeProfile().map { it?.toDomain() }

    /**
     * 미등록(`null`)은 실패가 아니다 — 캐시를 비우고 정상 종료한다. 조회가 실패하면 캐시를 그대로 두고 예외를
     * 전파한다. 네트워크가 끊겼다고 캐시를 비우면 오프라인에서 프리필이 사라진다.
     */
    override suspend fun refreshProfile() {
        val response = remoteDataSource.getMe()
        if (response == null) {
            localDataSource.clearProfile()
        } else {
            localDataSource.saveProfile(response.toDomain().toEntry())
        }
    }

    /**
     * 저장소 전체에서 등록/수정 분기는 이 한 곳이다 — 캐시가 비었으면 등록, 있으면 수정이다(D38).
     * `409`도 다른 실패와 같이 올라가며 수정으로 갈아타지 않는다(불변식 3).
     *
     * **원격 성공 → 캐시 갱신** 순서다. 원격이 던지면 캐시는 손대지 않은 채로 남는다(불변식 1 · FR-012·SC-006).
     */
    override suspend fun saveProfile(profile: Profile) {
        val request = profile.toRequest()
        val cached = localDataSource.observeProfile().first()
        val response =
            if (cached == null) {
                remoteDataSource.register(request)
            } else {
                remoteDataSource.updateMe(request)
            }
        localDataSource.saveProfile(response.toDomain().toEntry())
    }

    override suspend fun currentUserId(): String? = remoteDataSource.getMe()?.id
}
