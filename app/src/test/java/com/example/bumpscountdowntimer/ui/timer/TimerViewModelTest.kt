package com.example.bumpscountdowntimer.ui.timer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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
        assertEquals(false, viewModel.isHoldingFor4Min.value)
        
        viewModel.reset()
    }

    @Test
    fun `sync1Min sets remaining time to 1 minute and state to PREP_1_MIN`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()
        viewModel.sync1Min()
        
        assertEquals(60000L, viewModel.remainingMillis.value)
        assertEquals(TimerState.PREP_1_MIN, viewModel.timerState.value)
        assertEquals(false, viewModel.isHoldingFor4Min.value)
        
        viewModel.reset()
    }

    @Test
    fun `scheduled start counts down to 4-min gun and prompts immediately on expiration`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()
        val now = currentTime
        val startTime = now + 10.minutes.inWholeMilliseconds
        
        viewModel.setScheduledStartTime(startTime)
        
        assertEquals(TimerState.PRE_SEQUENCE, viewModel.timerState.value)
        assertEquals(6.minutes.inWholeMilliseconds, viewModel.remainingMillis.value)
        
        // Advance to 1s before 4-min gun
        advanceTimeBy(6.minutes.inWholeMilliseconds - 1000)
        runCurrent()
        assertEquals(1000L, viewModel.remainingMillis.value)
        
        // Advance past the 4-min gun time
        advanceTimeBy(2000)
        runCurrent()
        
        // Should enter ROLLING_HOLD and show prompt immediately (remainingMillis == 0)
        assertEquals(TimerState.ROLLING_HOLD, viewModel.timerState.value)
        assertEquals(true, viewModel.isHoldingFor4Min.value)
        assertEquals(0L, viewModel.remainingMillis.value)
        
        viewModel.reset()
    }

    @Test
    fun `confirming 4-min gun after delay starts 4-min sequence`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()
        val now = currentTime
        viewModel.setScheduledStartTime(now + 2.minutes.inWholeMilliseconds) // Already late for 4-min gun
        
        assertEquals(TimerState.ROLLING_HOLD, viewModel.timerState.value)
        assertEquals(true, viewModel.isHoldingFor4Min.value)
        assertEquals(0L, viewModel.remainingMillis.value)
        
        viewModel.onRollingHoldComplete(confirmed = true)
        
        assertEquals(TimerState.WARNING_4_MIN, viewModel.timerState.value)
        assertEquals(240000L, viewModel.remainingMillis.value)
        
        viewModel.reset()
    }

    @Test
    fun `startRollingHold from IDLE sets isHoldingFor4Min to true`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()
        assertEquals(TimerState.IDLE, viewModel.timerState.value)
        
        viewModel.startRollingHold()
        
        assertEquals(TimerState.ROLLING_HOLD, viewModel.timerState.value)
        assertEquals(true, viewModel.isHoldingFor4Min.value)
        
        viewModel.reset()
    }

    @Test
    fun `rolling hold loop pattern works`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()
        viewModel.startRollingHold()
        
        // Initial state after startRollingHold: 60s countdown
        assertEquals(60000L, viewModel.remainingMillis.value)
        
        advanceTimeBy(60100)
        runCurrent()
        
        // Reached 0, should show prompt
        assertEquals(0L, viewModel.remainingMillis.value)
        assertEquals(TimerState.ROLLING_HOLD, viewModel.timerState.value)
        
        // Selecting "No" starts another 60s loop
        viewModel.onRollingHoldComplete(confirmed = false)
        assertEquals(60000L, viewModel.remainingMillis.value)
        
        viewModel.reset()
    }
}
