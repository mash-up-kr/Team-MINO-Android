package team.mino.core.data.repository

import team.mino.core.data.datasource.HomePreferencesLocalDataSource
import team.mino.core.domain.repository.HomePreferencesRepository
import javax.inject.Inject

/**
 * 홈의 영속 값은 원격이 없어 DTO도 매퍼도 없다 — 로컬 DataSource 위임이 전부다
 * (`docs/specs/home-deck-exploration/contracts/home-ui.md` §4.3).
 *
 * 저장된 방이 없을 때 어느 방으로 시작할지(FR-022)는 여기서 정하지 않는다. 방 목록을 아는 쪽이 판단할 몫이라
 * `null`을 그대로 올려보낸다.
 *
 * 예외를 잡지 않는다. 저장 실패는 그대로 전파되고 소비는 ViewModel의 `runCatchingDomain`이 한다.
 */
internal class HomePreferencesRepositoryImpl @Inject constructor(
    private val localDataSource: HomePreferencesLocalDataSource,
) : HomePreferencesRepository {
    override suspend fun getLastRoomId(): String? = localDataSource.getLastRoomId()

    override suspend fun setLastRoomId(roomId: String) = localDataSource.setLastRoomId(roomId)

    override suspend fun isGuideDismissed(): Boolean = localDataSource.isGuideDismissed()

    override suspend fun dismissGuide() = localDataSource.dismissGuide()
}
