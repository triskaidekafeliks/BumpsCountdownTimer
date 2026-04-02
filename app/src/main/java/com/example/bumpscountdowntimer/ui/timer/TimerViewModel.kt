package com.example.bumpscountdowntimer.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerViewModel(
    private val clock: Clock = SystemClock()
) : ViewModel() {

    private val _remainingMillis = MutableStateFlow(0L)
    val remainingMillis: StateFlow<Long> = _remainingMillis.asStateFlow()

    private val _timerState = MutableStateFlow(TimerState.IDLE)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _hapticEvents = MutableSharedFlow<HapticType>(extraBufferCapacity = 10)
    val hapticEvents: SharedFlow<HapticType> = _hapticEvents.asSharedFlow()

    private var timerJob: Job? = null
    private var targetTimeMillis: Long = 0L

    private var lastHapticSecond: Long = -1L
    private var lastHapticMinute: Long = -1L

    fun sync4Min() {
        startSequence(4 * 60 * 1000L, TimerState.WARNING_4_MIN)
    }

    fun sync1Min() {
        startSequence(1 * 60 * 1000L, TimerState.PREP_1_MIN)
    }

    fun startRollingHold() {
        startSequence(60 * 1000L, TimerState.ROLLING_HOLD)
    }

    private fun startSequence(duration: Long, state: TimerState) {
        val now = clock.currentTimeMillis()
        targetTimeMillis = now + duration
        _timerState.value = state
        _remainingMillis.value = duration
        resetHapticTrackers()
        
        // Initial haptic if starting at a 60s boundary
        if (duration % 60000L == 0L) {
            lastHapticMinute = duration / 1000L
            triggerHaptic(HapticType.MARK_60S)
        }
        
        startTimerJob()
    }

    fun onRollingHoldComplete(confirmed: Boolean) {
        if (confirmed) {
            sync1Min()
        } else {
            startRollingHold()
        }
    }

    fun gunNow1Min() {
        sync1Min()
    }

    private fun resetHapticTrackers() {
        lastHapticSecond = -1L
        lastHapticMinute = -1L
    }

    private fun triggerHaptic(type: HapticType) {
        _hapticEvents.tryEmit(type)
    }

    private fun startTimerJob() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                val now = clock.currentTimeMillis()
                val currentState = _timerState.value
                
                // Exit if idle or already started (shouldn't happen here but for safety)
                if (currentState == TimerState.IDLE || currentState == TimerState.STARTED) {
                    break
                }

                val remaining = targetTimeMillis - now
                
                if (remaining <= 0) {
                    _remainingMillis.value = 0
                    if (currentState == TimerState.ROLLING_HOLD) {
                        // Stay in ROLLING_HOLD at 0, break loop to wait for user interaction
                        break 
                    } else {
                        _timerState.value = TimerState.STARTED
                        triggerHaptic(HapticType.START)
                        break
                    }
                } else {
                    _remainingMillis.value = remaining
                    
                    // Only transition states if not in ROLLING_HOLD
                    if (currentState != TimerState.ROLLING_HOLD) {
                        val newState = determineState(remaining)
                        _timerState.value = newState
                        checkHapticPulses(remaining, newState)
                    } else {
                        checkHapticPulses(remaining, TimerState.ROLLING_HOLD)
                    }
                }
                
                delay(100) // 10Hz tick
            }
        }
    }

    private fun checkHapticPulses(remaining: Long, state: TimerState) {
        val totalSeconds = (remaining + 999) / 1000
        
        // 60-second Pulse (Distinct vibration)
        if (totalSeconds > 0 && totalSeconds % 60 == 0L && totalSeconds != lastHapticMinute) {
            triggerHaptic(HapticType.MARK_60S)
            lastHapticMinute = totalSeconds
        }

        // Final 10-second Pulses (Per-second tick)
        if (state == TimerState.FINAL_COUNTDOWN) {
            if (totalSeconds in 1..10 && totalSeconds != lastHapticSecond) {
                triggerHaptic(HapticType.TICK_1S)
                lastHapticSecond = totalSeconds
            }
        }
    }

    private fun determineState(remaining: Long): TimerState {
        return when {
            remaining > 60000L -> TimerState.WARNING_4_MIN
            remaining > 10000L -> TimerState.PREP_1_MIN
            remaining > 0L -> TimerState.FINAL_COUNTDOWN
            else -> TimerState.STARTED
        }
    }

    fun reset() {
        timerJob?.cancel()
        _timerState.value = TimerState.IDLE
        _remainingMillis.value = 0
        resetHapticTrackers()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
