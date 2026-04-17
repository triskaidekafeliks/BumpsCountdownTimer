package com.example.bumpscountdowntimer.ui.timer

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatMillisTest {
    @Test
    fun testFormatMillis() {
        val state = TimerState.WARNING_4_MIN

        // Basic cases
        assertEquals("0:00", formatMillis(0, state))
        assertEquals("0:01", formatMillis(1, state))
        assertEquals("0:01", formatMillis(1000, state))
        assertEquals("0:02", formatMillis(1001, state))
        assertEquals("1:00", formatMillis(60000, state))
        assertEquals("1:01", formatMillis(60001, state))

        // Edge cases for minutes
        assertEquals("10:00", formatMillis(10 * 60 * 1000, state))
        assertEquals("100:00", formatMillis(100 * 60 * 1000, state))
        assertEquals("1000:00", formatMillis(1000 * 60 * 1000, state))

        // STARTED state
        assertEquals("START!", formatMillis(0, TimerState.STARTED))
    }
}
