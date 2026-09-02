package team.mino.core.domain.repository

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 방 리스트가 기기에 영속 저장하는 값의 계약.
 *
 * 대상은 공동방 생성 유도 Nudge 팝업([RoomNudgeAutoSheet])을 마지막으로 닫은 시각 하나뿐이다
 * (PRD 11.1.0 [SYS-009] — [나중에 만들래요] 클릭 시 2주 동안 재표출하지 않는다).
 *
 * "2주가 지났는지" 판정은 이 계약의 책임이 아니다 — `HomePreferencesRepository`의 FR-022
 * 판단이 호출부 몫이었던 것과 같은 이유로, 저장된 시각을 그대로 올려보내고 억제 기간과의 비교는
 * `RoomListViewModel`이 한다.
 */
@OptIn(ExperimentalTime::class)
interface RoomPreferencesRepository {
    /** Nudge 팝업을 마지막으로 닫은 시각. 닫은 적이 없으면 `null`. */
    suspend fun getNudgeDismissedAt(): Instant?

    /** Nudge 팝업을 [dismissedAt]에 닫은 것으로 기록한다. */
    suspend fun setNudgeDismissedAt(dismissedAt: Instant)
}
