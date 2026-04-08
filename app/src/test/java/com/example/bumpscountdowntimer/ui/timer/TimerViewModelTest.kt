package com.example.bumpscountdowntimer.ui.timer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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
    fun `confirming 4-min gun after delay removes latency from new timer`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()
        val gunTime = currentTime
        viewModel.setScheduledStartTime(gunTime + 4.minutes.inWholeMilliseconds) // Scheduled for now
        
        // It's exactly gun time, so it prompts immediately
        assertEquals(TimerState.ROLLING_HOLD, viewModel.timerState.value)
        assertEquals(0L, viewModel.remainingMillis.value)
        
        // Wait 30 seconds before confirming
        advanceTimeBy(30000)
        runCurrent()
        
        viewModel.onRollingHoldComplete(confirmed = true)
        
        // 4 minutes (240s) minus 30s latency = 210s remaining
        assertEquals(TimerState.WARNING_4_MIN, viewModel.timerState.value)
        assertEquals(210000L, viewModel.remainingMillis.value)
        
        viewModel.reset()
    }

    @Test
    fun `rejecting gun prompt (No) after delay removes latency from hold timer`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()
        viewModel.startRollingHold()
        
        // Initial 60s countdown
        assertEquals(60000L, viewModel.remainingMillis.value)
        
        // Let it run down and wait another 15s
        advanceTimeBy(75000)
        runCurrent()
        
        assertEquals(0L, viewModel.remainingMillis.value)
        
        // Press "No"
        viewModel.onRollingHoldComplete(confirmed = false)
        
        // New 60s hold minus 15s latency = 45s remaining
        assertEquals(TimerState.ROLLING_HOLD, viewModel.timerState.value)
        assertEquals(45000L, viewModel.remainingMillis.value)
        
        viewModel.reset()
    }

    @Test
    fun `pressing Rolling Hold button after gun time passed preserves the sync`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()
        // Set a target for 1 minute from now
        viewModel.sync1Min()
        
        // Wait until 5 seconds AFTER the 1-min gun was due (65s total)
        advanceTimeBy(65000)
        runCurrent()
        
        // Current state would be STARTED (because sync1Min doesn't go to ROLLING_HOLD automatically on expiration, 
        // it goes to STARTED). 
        // Wait, check sync1Min behavior in ViewModel:
        // startSequence(1 * 60 * 1000L, TimerState.PREP_1_MIN)
        // determineState for 0 or less returns STARTED.
        assertEquals(TimerState.STARTED, viewModel.timerState.value)
        
        // Now press "Rolling Hold"
        viewModel.startRollingHold()
        
        // Original gun was at T+60s. We are at T+65s.
        // Next sync point is T+120s.
        // Time remaining should be 120 - 65 = 55s.
        assertEquals(TimerState.ROLLING_HOLD, viewModel.timerState.value)
        assertEquals(55000L, viewModel.remainingMillis.value)
        
        viewModel.reset()
    }

    @Test
    fun `pressing Rolling Hold button before gun time preserves the sync`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()
        viewModel.sync1Min() // 60s remaining
        
        advanceTimeBy(10000) // 50s remaining
        runCurrent()
        
        viewModel.startRollingHold()
        
        // Should still be 50s remaining, but now in ROLLING_HOLD state
        assertEquals(TimerState.ROLLING_HOLD, viewModel.timerState.value)
        assertEquals(50000L, viewModel.remainingMillis.value)
        
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
    fun `hapticEvents emits 60s mark at minute boundaries`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()
        val events = mutableListOf<HapticType>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.hapticEvents.collect { events.add(it) }
        }

        // Start a 4 minute sequence
        viewModel.sync4Min() // Emits MARK_60S at 4m
        runCurrent()

        val expectedEvents = mutableListOf(HapticType.MARK_60S)
        assertEquals(expectedEvents, events)

        // Advance minute by minute and check for new events
        repeat(2) {
            advanceTimeBy(60000)
            runCurrent()
            expectedEvents.add(HapticType.MARK_60S)
            assertEquals(expectedEvents, events)
        }

        viewModel.reset()
    }

    @Test
    fun `hapticEvents emits tick 1s during final 10 seconds`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()
        val events = mutableListOf<HapticType>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.hapticEvents.collect { events.add(it) }
        }

        // Start 1 minute sequence
        viewModel.sync1Min()
        runCurrent()
        events.clear() // Clear initial MARK_60S

        // Advance to 1 second remaining, which should trigger all 10 ticks.
        advanceTimeBy(59000)
        runCurrent()

        // Verify that 10 TICK_1S events were emitted.
        val expectedEvents = List(10) { HapticType.TICK_1S }
        assertEquals(expectedEvents, events)

        viewModel.reset()
    }

    @Test
    fun `hapticEvents emits START at zero`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()
        val events = mutableListOf<HapticType>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.hapticEvents.collect { events.add(it) }
        }

        viewModel.sync1Min()
        runCurrent()
        events.clear() // Clear initial MARK_60S

        // Advance past zero
        advanceTimeBy(60000)
        runCurrent()

        // The last event should be START, and there should be 11 events total (10 ticks + 1 start).
        assertEquals(11, events.size)
        assertEquals(HapticType.START, events.last())

        viewModel.reset()
    }

    @Test
    fun `hapticEvents emits MARK_60S when PRE_SEQUENCE expires and transitions to ROLLING_HOLD`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()
        val events = mutableListOf<HapticType>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.hapticEvents.collect { events.add(it) }
        }

        val startTime = currentTime + 10.minutes.inWholeMilliseconds
        viewModel.setScheduledStartTime(startTime)

        // Wait until 4-min gun time
        advanceTimeBy(6.minutes.inWholeMilliseconds)
        runCurrent()

        // Assert that MARK_60S was emitted during the transition to ROLLING_HOLD
        assertEquals(TimerState.ROLLING_HOLD, viewModel.timerState.value)
        assertEquals(true, events.contains(HapticType.MARK_60S))

        viewModel.reset()
    }

    @Test
    fun `hapticEvents trackers are reset when a new sequence starts`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()
        val events = mutableListOf<HapticType>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.hapticEvents.collect { events.add(it) }
        }

        // Start 1 minute sequence and let it tick down to final 10 seconds
        viewModel.sync1Min()
        advanceTimeBy(59000)
        runCurrent()

        // Ensure TICK_1S events are in the list
        val hasTick1S = events.contains(HapticType.TICK_1S)
        assertEquals(true, hasTick1S)
        events.clear()

        // Restart 1 minute sequence to see if trackers were reset and TICK_1S can happen again
        viewModel.sync1Min()
        advanceTimeBy(59000)
        runCurrent()

        val hasTick1SAgain = events.contains(HapticType.TICK_1S)
        assertEquals(true, hasTick1SAgain)

        viewModel.reset()
    }

    @Test
    fun `no haptic events emitted in IDLE state`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()
        val events = mutableListOf<HapticType>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.hapticEvents.collect { events.add(it) }
        }

        // Initially in IDLE state
        assertEquals(TimerState.IDLE, viewModel.timerState.value)

        // Wait for some time to ensure no pulses happen in IDLE
        advanceTimeBy(120000)
        runCurrent()

        assertEquals(true, events.isEmpty())

        viewModel.reset()
    }

    @Test
    fun `reset resets all state flows to default values`() = runTest(timeout = 10.seconds) {
        val viewModel = createViewModel()

        // 1. Test reset from a state where isHoldingFor4Min is true
        viewModel.startRollingHold()
        assertEquals(TimerState.ROLLING_HOLD, viewModel.timerState.value)
        assertEquals(true, viewModel.isHoldingFor4Min.value)

        viewModel.reset()
        assertEquals(TimerState.IDLE, viewModel.timerState.value)
        assertEquals(0L, viewModel.remainingMillis.value)
        assertEquals(false, viewModel.isHoldingFor4Min.value)

        // 2. Test reset from a state with positive remainingMillis
        viewModel.sync4Min()
        assertEquals(TimerState.WARNING_4_MIN, viewModel.timerState.value)
        assertEquals(240000L, viewModel.remainingMillis.value)

        viewModel.reset()
        assertEquals(TimerState.IDLE, viewModel.timerState.value)
        assertEquals(0L, viewModel.remainingMillis.value)

        // 3. Ensure timer job is stopped (remainingMillis doesn't change after advanceTime)
        viewModel.sync1Min()
        viewModel.reset()
        advanceTimeBy(5000)
        runCurrent()
        assertEquals(0L, viewModel.remainingMillis.value)
        assertEquals(TimerState.IDLE, viewModel.timerState.value)
    }
}
