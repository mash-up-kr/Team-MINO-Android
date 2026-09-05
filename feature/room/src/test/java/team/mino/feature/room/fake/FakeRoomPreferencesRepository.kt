package team.mino.feature.room.fake

import team.mino.core.domain.repository.RoomPreferencesRepository
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * `:feature:room` 테스트용 [RoomPreferencesRepository] 테스트 더블. `RoomListViewModel` 테스트 전부가
 * 이 하나를 공유한다.
 *
 * 기본값은 **Nudge 팝업을 닫은 적이 없는 상태**다([dismissedAt] `null`) — 2주 억제(#290, PRD 11.1.0
 * [SYS-009])를 보려면 [dismissedAt]을 세우면 된다.
 *
 * [recordedDismissedAts]를 남기는 이유는 [FakeHomePreferencesRepository]와 같다 — 쓰기가 실제로
 * 일어났는지가 상태로 드러나지 않아, 여기서 세지 않으면 영속 저장을 빠뜨린 구현도 통과한다.
 */
@OptIn(ExperimentalTime::class)
internal class FakeRoomPreferencesRepository : RoomPreferencesRepository {
    /** Nudge 팝업을 마지막으로 닫은 시각. [setNudgeDismissedAt]이 덮어쓴다. */
    var dismissedAt: Instant? = null

    private val dismissedAtWrites = mutableListOf<Instant>()

    /** [setNudgeDismissedAt]으로 저장된 시각들. 호출 순서대로 쌓인다. */
    val recordedDismissedAts: List<Instant> get() = dismissedAtWrites

    override suspend fun getNudgeDismissedAt(): Instant? = dismissedAt

    override suspend fun setNudgeDismissedAt(dismissedAt: Instant) {
        this.dismissedAt = dismissedAt
        dismissedAtWrites += dismissedAt
    }
}
