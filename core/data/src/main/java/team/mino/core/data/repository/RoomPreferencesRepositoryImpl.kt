package team.mino.core.data.repository

import team.mino.core.data.datasource.RoomPreferencesLocalDataSource
import team.mino.core.domain.repository.RoomPreferencesRepository
import javax.inject.Inject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 방 리스트의 영속 값은 원격이 없어 DTO도 매퍼도 없다 — 로컬 DataSource 위임이 전부다
 * (`HomePreferencesRepositoryImpl`과 같은 이유).
 *
 * 2주 억제 판정을 여기서 하지 않는다 — 저장된 시각을 그대로 올려보내고, 억제 기간과의 비교는
 * `RoomListViewModel`의 몫이다.
 *
 * 예외를 잡지 않는다. 저장 실패는 그대로 전파되고 소비는 ViewModel의 `launchSafely`가 한다.
 */
@OptIn(ExperimentalTime::class)
internal class RoomPreferencesRepositoryImpl @Inject constructor(
    private val localDataSource: RoomPreferencesLocalDataSource,
) : RoomPreferencesRepository {
    override suspend fun getNudgeDismissedAt(): Instant? = localDataSource.getNudgeDismissedAt()

    override suspend fun setNudgeDismissedAt(dismissedAt: Instant) = localDataSource.setNudgeDismissedAt(dismissedAt)
}
