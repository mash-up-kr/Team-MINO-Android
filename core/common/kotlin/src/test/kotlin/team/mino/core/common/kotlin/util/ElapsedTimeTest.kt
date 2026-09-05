package team.mino.core.common.kotlin.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 경과 시간 구간 판정.
 *
 * 경계(1시간·24시간·7일)는 **경계값 자체가 다음 구간에 속한다** — 판정이 `<`이라 정확히 1시간 지난 항목은
 * `방금`이 아니라 `1시간 전`이다. 그래서 경계마다 「1초 모자란 값」과 「경계값」을 짝으로 찌른다.
 *
 * 시간대는 인자로 넘긴다. JVM 기본 시간대를 뒤집으면 같은 static을 쓰는 다른 테스트와 경합한다.
 */
@OptIn(ExperimentalTime::class)
class ElapsedTimeTest {
    @Test
    fun `1시간 미만은 방금이다`() {
        assertEquals(ElapsedTime.JustNow, elapsedTimeOf(20.minutes))
    }

    @Test
    fun `경과가 0이어도 방금이다`() {
        assertEquals(ElapsedTime.JustNow, elapsedTimeOf(Duration.ZERO))
    }

    @Test
    fun `1시간에서 1초 모자라면 아직 방금이다`() {
        assertEquals(ElapsedTime.JustNow, elapsedTimeOf(1.hours - 1.seconds))
    }

    @Test
    fun `정확히 1시간이면 1시간 전으로 넘어간다`() {
        assertEquals(ElapsedTime.HoursAgo(1), elapsedTimeOf(1.hours))
    }

    @Test
    fun `24시간 미만은 시간 단위로 내림한다`() {
        assertEquals(ElapsedTime.HoursAgo(5), elapsedTimeOf(5.hours + 59.minutes + 59.seconds))
    }

    @Test
    fun `24시간에서 1초 모자라면 아직 23시간 전이다`() {
        assertEquals(ElapsedTime.HoursAgo(23), elapsedTimeOf(1.days - 1.seconds))
    }

    @Test
    fun `정확히 24시간이면 1일 전으로 넘어간다`() {
        assertEquals(ElapsedTime.DaysAgo(1), elapsedTimeOf(1.days))
    }

    @Test
    fun `7일 미만은 일 단위로 내림한다`() {
        assertEquals(ElapsedTime.DaysAgo(3), elapsedTimeOf(3.days + 23.hours))
    }

    @Test
    fun `7일에서 1초 모자라면 아직 6일 전이다`() {
        assertEquals(ElapsedTime.DaysAgo(6), elapsedTimeOf(7.days - 1.seconds))
    }

    @Test
    fun `정확히 7일이면 절대 날짜로 넘어간다`() {
        val createdAt = Instant.parse("2027-01-01T03:00:00Z")

        assertEquals(
            ElapsedTime.AbsoluteDate(year = 2027, month = 1, day = 1),
            elapsedTime(createdAt = createdAt, observedAt = createdAt + 7.days, zone = seoul),
        )
    }

    /** 경과가 길어져도 발생일은 움직이지 않는다. */
    @Test
    fun `7일을 넘으면 발생일을 그대로 쓴다`() {
        val createdAt = Instant.parse("2027-08-10T03:00:00Z")

        assertEquals(
            ElapsedTime.AbsoluteDate(year = 2027, month = 8, day = 10),
            elapsedTime(createdAt = createdAt, observedAt = createdAt + 30.days, zone = seoul),
        )
    }

    /** 절대 날짜는 넘겨받은 시간대의 달력을 따른다 — UTC로 12월 31일인 시각이 KST에서는 1월 1일이다. */
    @Test
    fun `절대 날짜는 넘겨받은 시간대로 끊는다`() {
        val createdAt = Instant.parse("2026-12-31T16:00:00Z")
        val observedAt = createdAt + 30.days

        assertEquals(
            ElapsedTime.AbsoluteDate(year = 2027, month = 1, day = 1),
            elapsedTime(createdAt = createdAt, observedAt = observedAt, zone = seoul),
        )
        assertEquals(
            ElapsedTime.AbsoluteDate(year = 2026, month = 12, day = 31),
            elapsedTime(createdAt = createdAt, observedAt = observedAt, zone = ZoneId.of("UTC")),
        )
    }

    /** 기기 시각이 서버보다 뒤처져 발생 시각이 미래로 들어온다. */
    @Test
    fun `경과가 음수여도 방금으로 흡수한다`() {
        assertEquals(ElapsedTime.JustNow, elapsedTimeOf(-100.days))
    }

    private val seoul = ZoneId.of("Asia/Seoul")

    private fun elapsedTimeOf(elapsed: Duration): ElapsedTime {
        val observedAt = Instant.parse("2027-02-01T12:00:00Z")
        return elapsedTime(createdAt = observedAt - elapsed, observedAt = observedAt, zone = seoul)
    }
}
