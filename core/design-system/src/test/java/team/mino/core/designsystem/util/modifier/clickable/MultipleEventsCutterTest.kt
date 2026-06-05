package team.mino.core.designsystem.util.modifier.clickable

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MultipleEventsCutterTest {
    private var now: Long = 0L

    private fun cutter(intervalMillis: Long = 300L) =
        MultipleEventsCutter(intervalMillis = intervalMillis, currentTimeMillis = { now })

    @Test
    fun `첫 이벤트는 항상 통과한다`() {
        val cutter = cutter()
        now = 0L

        assertTrue(cutter.processEvent())
    }

    @Test
    fun `통과 직후 간격 미만의 이벤트는 차단한다`() {
        val cutter = cutter(intervalMillis = 300L)
        now = 0L
        cutter.processEvent()

        now = 200L

        assertFalse(cutter.processEvent())
    }

    @Test
    fun `간격 경계에 정확히 도달하면 통과한다`() {
        val cutter = cutter(intervalMillis = 300L)
        now = 0L
        cutter.processEvent()

        now = 300L

        assertTrue(cutter.processEvent())
    }

    @Test
    fun `간격을 초과하면 통과한다`() {
        val cutter = cutter(intervalMillis = 300L)
        now = 0L
        cutter.processEvent()

        now = 301L

        assertTrue(cutter.processEvent())
    }

    @Test
    fun `차단된 이벤트는 창을 연장하지 않는다`() {
        val cutter = cutter(intervalMillis = 300L)
        now = 0L
        assertTrue(cutter.processEvent())

        now = 200L
        assertFalse(cutter.processEvent())

        now = 350L
        assertTrue(cutter.processEvent())
    }

    @Test
    fun `연속 연타는 첫 통과 이후 모두 차단한다`() {
        val cutter = cutter(intervalMillis = 300L)
        now = 0L
        assertTrue(cutter.processEvent())

        now = 50L
        assertFalse(cutter.processEvent())
        now = 100L
        assertFalse(cutter.processEvent())
        now = 150L
        assertFalse(cutter.processEvent())
    }

    @Test
    fun `간격 이상 벌어진 연속 이벤트는 모두 통과한다`() {
        val cutter = cutter(intervalMillis = 300L)

        now = 0L
        assertTrue(cutter.processEvent())
        now = 300L
        assertTrue(cutter.processEvent())
        now = 600L
        assertTrue(cutter.processEvent())
    }

    @Test
    fun `커스텀 간격을 존중한다`() {
        val cutter = cutter(intervalMillis = 1000L)
        now = 0L
        assertTrue(cutter.processEvent())

        now = 999L
        assertFalse(cutter.processEvent())

        now = 1000L
        assertTrue(cutter.processEvent())
    }

    @Test
    fun `기본 간격은 300ms 이다`() {
        assertEquals(300L, MultipleEventsCutter.DEFAULT_INTERVAL_MILLIS)

        val cutter = MultipleEventsCutter(currentTimeMillis = { now })
        now = 0L
        assertTrue(cutter.processEvent())
        now = 299L
        assertFalse(cutter.processEvent())
        now = 300L
        assertTrue(cutter.processEvent())
    }

    @Test
    fun `같은 시각의 반복 이벤트는 첫 통과 이후 차단한다`() {
        val cutter = cutter(intervalMillis = 300L)
        now = 1_000L

        assertTrue(cutter.processEvent())
        assertFalse(cutter.processEvent())
        assertFalse(cutter.processEvent())
    }
}
