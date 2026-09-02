package team.mino.feature.room.placedetail.model

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.TimeZone
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 코멘트 작성 시각 구간 판정(spec FR-028·EC-028·EC-029, TS-050~TS-054).
 *
 * 경계(1시간·24시간·7일)는 **경계값 자체가 다음 구간에 속한다** — 판정이 `<`이라 정확히 1시간 지난 코멘트는
 * `방금`이 아니라 `1시간 전`이다.
 *
 * 7일 이상 갈래만 기기 시간대를 타므로 기본 시간대를 고정해 둔다. 나머지 세 갈래는 [Instant] 차이라 무관하다.
 */
@OptIn(ExperimentalTime::class)
class PlaceCommentTimeTest {
    private val defaultTimeZone = TimeZone.getDefault()

    @Before
    fun fixTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
    }

    @After
    fun restoreTimeZone() {
        TimeZone.setDefault(defaultTimeZone)
    }

    @Test
    fun `1시간 미만은 방금이다`() {
        assertEquals(PlaceCommentTime.JustNow, timeOf(20.minutes))
    }

    @Test
    fun `경과가 0이어도 방금이다`() {
        assertEquals(PlaceCommentTime.JustNow, timeOf(0.seconds))
    }

    @Test
    fun `1시간에서 1초 모자라면 아직 방금이다`() {
        assertEquals(PlaceCommentTime.JustNow, timeOf(1.hours - 1.seconds))
    }

    @Test
    fun `정확히 1시간이면 1시간 전으로 넘어간다`() {
        assertEquals(PlaceCommentTime.HoursAgo(1), timeOf(1.hours))
    }

    @Test
    fun `24시간 미만은 시간 단위로 내림한다`() {
        assertEquals(PlaceCommentTime.HoursAgo(3), timeOf(3.hours + 59.minutes))
    }

    @Test
    fun `24시간에서 1초 모자라면 아직 23시간 전이다`() {
        assertEquals(PlaceCommentTime.HoursAgo(23), timeOf(1.days - 1.seconds))
    }

    @Test
    fun `정확히 24시간이면 1일 전으로 넘어간다`() {
        assertEquals(PlaceCommentTime.DaysAgo(1), timeOf(1.days))
    }

    @Test
    fun `7일 미만은 일 단위로 내림한다`() {
        assertEquals(PlaceCommentTime.DaysAgo(3), timeOf(3.days + 23.hours))
    }

    @Test
    fun `7일에서 1초 모자라면 아직 6일 전이다`() {
        assertEquals(PlaceCommentTime.DaysAgo(6), timeOf(7.days - 1.seconds))
    }

    @Test
    fun `정확히 7일이면 절대 날짜로 넘어간다`() {
        val createdAt = Instant.parse("2027-01-01T03:00:00Z")
        val observedAt = createdAt + 7.days

        assertEquals(
            PlaceCommentTime.AbsoluteDate(year = 2027, month = 1, day = 1),
            placeCommentTime(createdAt = createdAt, observedAt = observedAt),
        )
    }

    /** TS-053 — 2027년 1월 1일에 쓴 코멘트를 30일 뒤에 본다. */
    @Test
    fun `7일을 넘으면 작성일을 그대로 쓴다`() {
        val createdAt = Instant.parse("2027-01-01T03:00:00Z")

        assertEquals(
            PlaceCommentTime.AbsoluteDate(year = 2027, month = 1, day = 1),
            placeCommentTime(createdAt = createdAt, observedAt = createdAt + 30.days),
        )
    }

    /** 절대 날짜는 기기 시간대의 달력을 따른다 — UTC로 12월 31일인 시각이 KST에서는 1월 1일이다. */
    @Test
    fun `절대 날짜는 기기 시간대로 끊는다`() {
        val createdAt = Instant.parse("2026-12-31T16:00:00Z")

        assertEquals(
            PlaceCommentTime.AbsoluteDate(year = 2027, month = 1, day = 1),
            placeCommentTime(createdAt = createdAt, observedAt = createdAt + 30.days),
        )
    }

    /** EC-029 — 기기 시각이 서버보다 뒤처져 작성 시각이 미래로 들어온다. */
    @Test
    fun `경과가 음수여도 방금으로 흡수한다`() {
        assertEquals(PlaceCommentTime.JustNow, timeOf(-30.minutes))
    }

    @Test
    fun `크게 앞선 미래 시각도 방금이다`() {
        assertEquals(PlaceCommentTime.JustNow, timeOf(-100.days))
    }

    private fun timeOf(elapsed: kotlin.time.Duration): PlaceCommentTime {
        val observedAt = Instant.parse("2027-02-01T12:00:00Z")
        return placeCommentTime(createdAt = observedAt - elapsed, observedAt = observedAt)
    }
}
