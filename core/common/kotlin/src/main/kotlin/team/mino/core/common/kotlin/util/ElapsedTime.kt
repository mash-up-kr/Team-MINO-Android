@file:OptIn(ExperimentalTime::class)

package team.mino.core.common.kotlin.util

import java.time.ZoneId
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

/**
 * 어떤 시각이 지금으로부터 얼마나 지났는지를 어느 구간으로 볼지 고른 결과.
 *
 * **문구 자체는 담지 않는다.** 어느 갈래인지와 그 갈래가 쓰는 수만 들고, 문자열은 이것을 받는 쪽이 자기 모듈의
 * 리소스에서 꺼낸다 — 구간을 나누는 규칙은 화면끼리 같지만 표기는 다를 수 있기 때문이다(알림함은 `N월 N일`,
 * 코멘트는 연도까지 쓴다). 판정만 순수 함수로 남겨야 경계값을 기기 없이 확인할 수 있다는 것도 같은 이유다.
 */
sealed interface ElapsedTime {
    /** 1시간 미만. */
    data object JustNow : ElapsedTime

    /** 1시간 이상 24시간 미만. */
    data class HoursAgo(val hours: Int) : ElapsedTime

    /** 24시간 이상 7일 미만. */
    data class DaysAgo(val days: Int) : ElapsedTime

    /**
     * 7일 이상 — 경과가 아니라 발생한 날짜를 그린다.
     *
     * [year]를 함께 드는 것은 연도를 쓰는 소비자가 있기 때문이다. 쓰지 않는 쪽은 무시하면 되고, 여기서 빼면
     * 연도가 필요한 쪽이 원본 시각을 다시 붙들어야 한다.
     */
    data class AbsoluteDate(val year: Int, val month: Int, val day: Int) : ElapsedTime
}

/**
 * 경과 시간을 네 구간 중 하나로 끊는다.
 *
 * **읽는 값을 전부 인자로 받는 순수 함수다.** 「지금」을 여기서 읽지 않고 [observedAt]으로 받는다 — 함수가 현재
 * 시각을 직접 읽으면 그리는 때마다 결과가 달라져 「화면에 머무는 동안 다시 계산하지 않는다」를 확인할 수단이
 * 사라진다. [observedAt]은 목록을 받은 시점에 한 번 읽은 값이며, 갱신은 목록을 다시 불러올 때 함께 일어난다.
 *
 * [zone]도 같은 이유로 인자다. 7일 이상 갈래만 달력을 보므로 시간대가 필요한데, 기본값을 함수 안에서 읽으면
 * 그 갈래만 숨은 입력을 갖게 되고 확인하려는 쪽이 JVM 전역 기본 시간대를 뒤집어야 한다. 기본값이 기기 시간대인
 * 것은 사용자가 보는 달력과 같기 때문이다.
 *
 * 네 구간의 경계값은 **경계 자체가 다음 구간에 속한다** — 판정이 `<`이라 정확히 60분이 지난 항목은 `방금`이
 * 아니라 `1시간 전`이다.
 *
 * 기기 시각이 서버보다 뒤처져 [createdAt]이 미래로 들어와도 경과가 1시간 미만 갈래로 흡수돼 음수 표기가 새어
 * 나가지 않는다.
 */
fun elapsedTime(
    createdAt: Instant,
    observedAt: Instant,
    zone: ZoneId = ZoneId.systemDefault(),
): ElapsedTime {
    val elapsed = observedAt - createdAt
    return when {
        elapsed < 1.hours -> ElapsedTime.JustNow
        elapsed < 1.days -> ElapsedTime.HoursAgo(elapsed.inWholeHours.toInt())
        elapsed < 7.days -> ElapsedTime.DaysAgo(elapsed.inWholeDays.toInt())
        else -> {
            val date = createdAt.toJavaInstant().atZone(zone).toLocalDate()
            ElapsedTime.AbsoluteDate(
                year = date.year,
                month = date.monthValue,
                day = date.dayOfMonth,
            )
        }
    }
}
