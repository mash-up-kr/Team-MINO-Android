package team.mino.core.data.repository

import team.mino.core.data.datasource.ProfileLocalDataSource
import team.mino.core.data.datasource.UserRemoteDataSource
import team.mino.core.domain.repository.ProfileRegistrationRepository
import javax.inject.Inject

internal class ProfileRegistrationRepositoryImpl @Inject constructor(
    private val userRemoteDataSource: UserRemoteDataSource,
    private val profileLocalDataSource: ProfileLocalDataSource,
) : ProfileRegistrationRepository {
    /**
     * **미등록이면 프로필 캐시를 비운다.** 조회에 쓰기가 딸린 것은 편의가 아니라 이 판정이 캐시를 무효로
     * 만들기 때문이다 — 서버가 모르는 세션의 캐시는 정의상 맞지 않는 값이고, 남겨 두면 두 곳이 어긋난다.
     *
     * 온보딩 화면이 진입 시 갱신을 걸지 않는 근거가 여기다(`ProfileEntryPoint.needsRefresh`). 그 화면은
     * 캐시가 비어 있음을 **전제**하는데, 그 전제를 세우는 것이 이 한 줄이다. 없으면 `saveProfile()`의
     * 등록/수정 분기가 캐시를 보고 `PATCH`로 갈라져, 서버가 모르는 유저에게 수정 요청이 나간다.
     *
     * 등록된 경우에는 캐시를 건드리지 않는다 — 값을 채우는 것은 `ProfileRepository.refreshProfile()`의 몫이고,
     * 이 판정은 값을 다루지 않는다.
     */
    override suspend fun isRegistered(): Boolean {
        val registered = userRemoteDataSource.isRegistered()
        if (!registered) profileLocalDataSource.clearProfile()
        return registered
    }
}
