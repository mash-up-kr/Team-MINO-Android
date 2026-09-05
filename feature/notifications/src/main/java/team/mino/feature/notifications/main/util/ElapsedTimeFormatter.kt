@file:OptIn(ExperimentalTime::class)

package team.mino.feature.notifications.main.util

import androidx.compose.runtime.Immutable
import java.time.ZoneId
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

/**
 * 알림이 발생한 지 얼마나 지났는지를 어느 구간으로 볼지 고른 결과(spec FR-003).
 *
 * **문구 자체는 담지 않는다.** 어느 갈래인지와 그 갈래가 쓰는 수만 들고, 문자열은 이것을 받는 컴포저블이
 * `:feature:notifications`의 리소스에서 꺼낸다 — 판정만 순수 함수로 남겨야 경계값을 기기 없이 확인할 수 있다
 * (spec SC-005).
 */
@Immutable
internal sealed interface ElapsedTime {
    /** 1시간 미만. */
    data object JustNow : ElapsedTime

    /** 1시간 이상 24시간 미만. */
    data class HoursAgo(val hours: Int) : ElapsedTime

    /** 24시간 이상 7일 미만. */
    data class DaysAgo(val days: Int) : ElapsedTime

    /** 7일 이상 — 경과가 아니라 발생한 날짜를 그린다. */
    data class AbsoluteDate(val month: Int, val day: Int) : ElapsedTime
}

/**
 * 경과 시간을 FR-003의 네 구간 중 하나로 끊는다.
 *
 * **입력이 둘인 순수 함수다.** 「지금」을 여기서 읽지 않고 [observedAt]으로 받는다 — 함수가 현재 시각을 직접
 * 읽으면 그리는 때마다 결과가 달라져 "화면에 머무는 동안 다시 계산하지 않는다"(spec EC-005)를 확인할 수단이
 * 사라진다. [observedAt]은 목록을 받은 시점에 한 번 읽은 값이며, 갱신은 목록을 다시 불러올 때 함께 일어난다
 * (`docs/specs/notifications/research.md` D12).
 *
 * 네 구간의 경계값은 **경계 자체가 다음 구간에 속한다** — 판정이 `<`이라 정확히 60분이 지난 알림은 `방금`이
 * 아니라 `1시간 전`이다(spec SC-005).
 *
 * 기기 시각이 서버보다 뒤처져 [createdAt]이 미래로 들어와도 경과가 1시간 미만 갈래로 흡수돼 음수 표기가 새어
 * 나가지 않는다.
 *
 * 7일 이상만 절대 날짜라 시간대가 필요하다. 기기의 시간대로 끊는 것이 사용자가 보는 달력과 같다 — 나머지 세
 * 구간은 [Instant] 차이라 시간대의 영향을 받지 않는다.
 */
internal fun elapsedTime(
    createdAt: Instant,
    observedAt: Instant,
): ElapsedTime {
    val elapsed = observedAt - createdAt
    return when {
        elapsed < 1.hours -> ElapsedTime.JustNow
        elapsed < 1.days -> ElapsedTime.HoursAgo(elapsed.inWholeHours.toInt())
        elapsed < 7.days -> ElapsedTime.DaysAgo(elapsed.inWholeDays.toInt())
        else -> {
            val date = createdAt.toJavaInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            ElapsedTime.AbsoluteDate(month = date.monthValue, day = date.dayOfMonth)
        }
    }
}
