package team.mino.core.data.datasource

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 방 리스트가 기기에 남기는 값의 저장소.
 *
 * **영속 대상은 Nudge 팝업을 마지막으로 닫은 시각 하나뿐이다** — `HomePreferencesLocalDataSource`와
 * 같은 이유로, 2주 억제 판정은 여기서 하지 않고 위 레이어(`RoomListViewModel`)가 한다.
 */
@OptIn(ExperimentalTime::class)
internal interface RoomPreferencesLocalDataSource {
    /** Nudge 팝업을 마지막으로 닫은 시각. 닫은 적이 없으면 `null`. */
    suspend fun getNudgeDismissedAt(): Instant?

    suspend fun setNudgeDismissedAt(dismissedAt: Instant)
}
