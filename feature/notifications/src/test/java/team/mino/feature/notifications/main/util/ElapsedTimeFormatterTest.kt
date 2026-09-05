package team.mino.feature.notifications.main.util

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.TimeZone
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 알림 경과 시간 구간 판정(spec FR-003·SC-005·EC-005).
 *
 * SC-005가 지목한 경계값 여섯(59분 / 60분 / 23시간 59분 / 24시간 / 6일 23시간 / 7일)을 각각 한 케이스로
 * 세운다. 경계는 **경계값 자체가 다음 구간에 속한다** — 판정이 `<`이라 정확히 60분이 지난 알림은 `방금`이
 * 아니라 `1시간 전`이다.
 *
 * 7일 이상 갈래만 기기 시간대를 타므로 기본 시간대를 고정해 둔다. 나머지 세 갈래는 [Instant] 차이라 무관하다.
 */
@OptIn(ExperimentalTime::class)
class ElapsedTimeFormatterTest {
    private val defaultTimeZone: TimeZone = TimeZone.getDefault()

    @Before
    fun fixTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
    }

    @After
    fun restoreTimeZone() {
        TimeZone.setDefault(defaultTimeZone)
    }

    /** SC-005 경계 ① 59분 — 아직 `방금`이다. */
    @Test
    fun `59분은 방금이다`() {
        assertEquals(ElapsedTime.JustNow, elapsedTimeOf(59.minutes))
    }

    /** SC-005 경계 ② 60분 — `1시간 전`으로 넘어간다. */
    @Test
    fun `60분이면 1시간 전으로 넘어간다`() {
        assertEquals(ElapsedTime.HoursAgo(1), elapsedTimeOf(60.minutes))
    }

    /** SC-005 경계 ③ 23시간 59분 — 아직 `23시간 전`이다. */
    @Test
    fun `23시간 59분은 23시간 전이다`() {
        assertEquals(ElapsedTime.HoursAgo(23), elapsedTimeOf(23.hours + 59.minutes))
    }

    /** SC-005 경계 ④ 24시간 — `1일 전`으로 넘어간다. */
    @Test
    fun `24시간이면 1일 전으로 넘어간다`() {
        assertEquals(ElapsedTime.DaysAgo(1), elapsedTimeOf(24.hours))
    }

    /** SC-005 경계 ⑤ 6일 23시간 — 아직 `6일 전`이다. */
    @Test
    fun `6일 23시간은 6일 전이다`() {
        assertEquals(ElapsedTime.DaysAgo(6), elapsedTimeOf(6.days + 23.hours))
    }

    /** SC-005 경계 ⑥ 7일 — 절대 날짜로 넘어간다. */
    @Test
    fun `7일이면 절대 날짜로 넘어간다`() {
        val createdAt = Instant.parse("2027-01-01T03:00:00Z")

        assertEquals(
            ElapsedTime.AbsoluteDate(month = 1, day = 1),
            elapsedTime(createdAt = createdAt, observedAt = createdAt + 7.days),
        )
    }

    @Test
    fun `경과가 0이어도 방금이다`() {
        assertEquals(ElapsedTime.JustNow, elapsedTimeOf(Duration.ZERO))
    }

    @Test
    fun `24시간 미만은 시간 단위로 내림한다`() {
        assertEquals(ElapsedTime.HoursAgo(5), elapsedTimeOf(5.hours + 59.minutes + 59.seconds))
    }

    @Test
    fun `7일 미만은 일 단위로 내림한다`() {
        assertEquals(ElapsedTime.DaysAgo(3), elapsedTimeOf(3.days + 12.hours))
    }

    /** TS-007 — 8일 전에 발생한 알림은 발생한 날짜를 그대로 쓴다. */
    @Test
    fun `7일을 넘으면 발생일을 그대로 쓴다`() {
        val createdAt = Instant.parse("2027-08-10T03:00:00Z")

        assertEquals(
            ElapsedTime.AbsoluteDate(month = 8, day = 10),
            elapsedTime(createdAt = createdAt, observedAt = createdAt + 8.days),
        )
    }

    /** 절대 날짜는 기기 시간대의 달력을 따른다 — UTC로 12월 31일인 시각이 KST에서는 1월 1일이다. */
    @Test
    fun `절대 날짜는 기기 시간대로 끊는다`() {
        val createdAt = Instant.parse("2026-12-31T16:00:00Z")

        assertEquals(
            ElapsedTime.AbsoluteDate(month = 1, day = 1),
            elapsedTime(createdAt = createdAt, observedAt = createdAt + 30.days),
        )
    }

    /** 기기 시각이 서버보다 뒤처져 발생 시각이 미래로 들어와도 음수 표기가 새어 나가지 않는다. */
    @Test
    fun `경과가 음수여도 방금으로 흡수한다`() {
        assertEquals(ElapsedTime.JustNow, elapsedTimeOf(-30.minutes))
    }

    private fun elapsedTimeOf(elapsed: Duration): ElapsedTime {
        val observedAt = Instant.parse("2027-02-01T12:00:00Z")
        return elapsedTime(createdAt = observedAt - elapsed, observedAt = observedAt)
    }
}
