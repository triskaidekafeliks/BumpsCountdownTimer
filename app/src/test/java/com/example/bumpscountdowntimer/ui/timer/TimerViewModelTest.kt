package com.example.bumpscountdowntimer.ui.timer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

/**
 * A Clock implementation that is synced with the TestCoroutineScheduler's virtual time.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TestClock(private val scheduler: TestCoroutineScheduler) : Clock {
    override fun currentTimeMillis(): Long = scheduler.currentTime
}

@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.createViewModel(): TimerViewModel {
        return TimerViewModel(TestClock(testScheduler))
    }

    @Test
    fun `sync4Min sets remaining time to 4 minutes and state to WARNING_4_MIN`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()
        viewModel.sync4Min()
        
        assertEquals(240000L, viewModel.remainingMillis.value)
        assertEquals(TimerState.WARNING_4_MIN, viewModel.timerState.value)
        
        viewModel.reset()
    }

    @Test
    fun `sync1Min sets remaining time to 1 minute and state to PREP_1_MIN`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()
        viewModel.sync1Min()
        
        assertEquals(60000L, viewModel.remainingMillis.value)
        assertEquals(TimerState.PREP_1_MIN, viewModel.timerState.value)
        
        viewModel.reset()
    }

    @Test
    fun `timer transitions states as time passes`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()
        viewModel.sync1Min() // 60s
        
        // Advance virtual time to 9s remaining
        advanceTimeBy(51000)
        runCurrent()
        
        assertEquals(TimerState.FINAL_COUNTDOWN, viewModel.timerState.value)
        assertEquals(9000L, viewModel.remainingMillis.value)
        
        // Advance to 0s
        advanceTimeBy(10000) // 10s more to be safe (9s + extra)
        runCurrent()
        
        assertEquals(TimerState.STARTED, viewModel.timerState.value)
        assertEquals(0L, viewModel.remainingMillis.value)
    }

    @Test
    fun `rolling hold stops at 0 and breaks loop`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()
        viewModel.startRollingHold()
        
        assertEquals(TimerState.ROLLING_HOLD, viewModel.timerState.value)
        assertEquals(60000L, viewModel.remainingMillis.value)
        
        advanceTimeBy(60100)
        runCurrent()
        
        assertEquals(0L, viewModel.remainingMillis.value)
        assertEquals(TimerState.ROLLING_HOLD, viewModel.timerState.value)
    }

    @Test
    fun `rolling hold restarts when not confirmed`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()
        viewModel.startRollingHold()
        
        advanceTimeBy(60100)
        runCurrent()
        
        viewModel.onRollingHoldComplete(confirmed = false)
        
        assertEquals(60000L, viewModel.remainingMillis.value)
        assertEquals(TimerState.ROLLING_HOLD, viewModel.timerState.value)
        
        viewModel.reset()
    }

    @Test
    fun `rolling hold transitions to prep 1 min when confirmed`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()
        viewModel.startRollingHold()
        
        advanceTimeBy(60100)
        runCurrent()
        
        viewModel.onRollingHoldComplete(confirmed = true)
        
        assertEquals(60000L, viewModel.remainingMillis.value)
        assertEquals(TimerState.PREP_1_MIN, viewModel.timerState.value)
        
        viewModel.reset()
    }
}
