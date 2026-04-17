package com.example.bumpscountdowntimer.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerViewModel(
    private val clock: Clock = SystemClock()
) : ViewModel() {

    companion object {
        private const val FOUR_MINUTES_MILLIS = 4 * 60 * 1000L
        private const val ONE_MINUTE_MILLIS = 60 * 1000L
        private const val TEN_SECONDS_MILLIS = 10 * 1000L
    }

    private val _remainingMillis = MutableStateFlow(0L)
    val remainingMillis: StateFlow<Long> = _remainingMillis.asStateFlow()

    /**
     * A throttled version of [remainingMillis] that only updates when the visible
     * second changes. This is used by the UI to minimize recompositions.
     */
    val timerDisplayMillis: StateFlow<Long> = _remainingMillis
        .map { (it + 999) / 1000 * 1000 }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _remainingMillis.value
        )

    private val _timerState = MutableStateFlow(TimerState.IDLE)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _hapticEvents = MutableSharedFlow<HapticType>(extraBufferCapacity = 10)
    val hapticEvents: SharedFlow<HapticType> = _hapticEvents.asSharedFlow()

    private val _isHoldingFor4Min = MutableStateFlow(false)
    val isHoldingFor4Min: StateFlow<Boolean> = _isHoldingFor4Min.asStateFlow()

    private var timerJob: Job? = null
    private var targetTimeMillis: Long = 0L

    private var lastHapticSecond: Long = -1L
    private var lastHapticMinute: Long = -1L

    fun setScheduledStartTime(startTimeMillis: Long) {
        val now = clock.currentTimeMillis()
        val fourMinGunTime = startTimeMillis - FOUR_MINUTES_MILLIS
        targetTimeMillis = fourMinGunTime
        
        if (fourMinGunTime <= now) {
            // Past scheduled time, enter rolling hold at 0 and prompt immediately
            _isHoldingFor4Min.value = true
            _remainingMillis.value = 0
            _timerState.value = TimerState.ROLLING_HOLD
        } else {
            _timerState.value = TimerState.PRE_SEQUENCE
            _isHoldingFor4Min.value = false
            _remainingMillis.value = (targetTimeMillis - now).coerceAtLeast(0)
        }
        
        resetHapticTrackers()
        startTimerJob()
    }

    fun sync4Min() {
        _isHoldingFor4Min.value = false
        startSequence(FOUR_MINUTES_MILLIS, TimerState.WARNING_4_MIN)
    }

    fun sync1Min() {
        _isHoldingFor4Min.value = false
        startSequence(ONE_MINUTE_MILLIS, TimerState.PREP_1_MIN)
    }

    private fun updateHoldState(currentState: TimerState) {
        if (currentState != TimerState.ROLLING_HOLD) {
            _isHoldingFor4Min.value = (currentState == TimerState.IDLE ||
                                      currentState == TimerState.PRE_SEQUENCE)
        }
    }

    fun startRollingHold() {
        val currentState = _timerState.value
        val now = clock.currentTimeMillis()

        // If we start hold while waiting for the sequence to begin or at the very start, it's for the 4-min gun.
        updateHoldState(currentState)
        
        val duration = calculateHoldDuration(currentState, now)
        startSequence(duration, TimerState.ROLLING_HOLD, now)
    }

    private fun calculateHoldDuration(currentState: TimerState, now: Long): Long {
        return if (currentState == TimerState.IDLE) {
            ONE_MINUTE_MILLIS
        } else {
            // Preserve the "phase" of the seconds relative to the current target
            val newTarget = calculateNextTargetTime(now)
            // If newTarget is in the future, we keep it. This ensures that if we press 
            // "Rolling Hold" 5s before a gun, the prompt still appears at the original time.
            newTarget - now
        }
    }

    private fun calculateNextTargetTime(now: Long): Long {
        var newTarget = targetTimeMillis
        // If we are already past the target (e.g. 5s late), move to the next minute mark
        if (newTarget <= now) {
            newTarget += ((now - newTarget) / ONE_MINUTE_MILLIS + 1) * ONE_MINUTE_MILLIS
        }
        return newTarget
    }

    private fun startSequence(duration: Long, state: TimerState, baseTimeMillis: Long? = null) {
        val now = clock.currentTimeMillis()
        val base = baseTimeMillis ?: now
        targetTimeMillis = base + duration
        _remainingMillis.value = (targetTimeMillis - now).coerceAtLeast(0)
        _timerState.value = state
        resetHapticTrackers()
        
        val totalSeconds = (duration + 999) / 1000
        if (totalSeconds > 0 && totalSeconds % 60 == 0L) {
            lastHapticMinute = totalSeconds
            triggerHaptic(HapticType.MARK_60S)
        }
        
        startTimerJob()
    }

    fun onRollingHoldComplete(confirmed: Boolean) {
        val baseTime = targetTimeMillis
        if (confirmed) {
            if (_isHoldingFor4Min.value) {
                startSequence(FOUR_MINUTES_MILLIS, TimerState.WARNING_4_MIN, baseTime)
            } else {
                startSequence(ONE_MINUTE_MILLIS, TimerState.PREP_1_MIN, baseTime)
            }
            _isHoldingFor4Min.value = false
        } else {
            // Start a 60s holding pattern loop, removing latency
            startSequence(ONE_MINUTE_MILLIS, TimerState.ROLLING_HOLD, baseTime)
        }
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
                
                if (currentState == TimerState.IDLE || currentState == TimerState.STARTED) {
                    break
                }

                val remaining = targetTimeMillis - now
                
                if (remaining <= 0) {
                    _remainingMillis.value = 0
                    if (currentState == TimerState.ROLLING_HOLD) {
                        // Prompt is visible (at remainingMillis == 0), wait for user confirmation
                        break 
                    } else if (currentState == TimerState.PRE_SEQUENCE) {
                        // Transition to Rolling Hold and show prompt immediately
                        _isHoldingFor4Min.value = true
                        _timerState.value = TimerState.ROLLING_HOLD
                        triggerHaptic(HapticType.MARK_60S) // Alert user that gun time has passed
                        break
                    } else {
                        _timerState.value = TimerState.STARTED
                        triggerHaptic(HapticType.START)
                        break
                    }
                } else {
                    _remainingMillis.value = remaining
                    
                    if (currentState != TimerState.ROLLING_HOLD && currentState != TimerState.PRE_SEQUENCE) {
                        val newState = determineState(remaining)
                        _timerState.value = newState
                        checkHapticPulses(remaining, newState)
                    } else {
                        checkHapticPulses(remaining, currentState)
                    }
                }
                
                delay(100)
            }
        }
    }

    private fun checkHapticPulses(remaining: Long, state: TimerState) {
        val totalSeconds = (remaining + 999) / 1000
        
        if (totalSeconds > 0 && totalSeconds % 60 == 0L && totalSeconds != lastHapticMinute) {
            triggerHaptic(HapticType.MARK_60S)
            lastHapticMinute = totalSeconds
        }

        if (state == TimerState.FINAL_COUNTDOWN) {
            if (totalSeconds in 1..10 && totalSeconds != lastHapticSecond) {
                triggerHaptic(HapticType.TICK_1S)
                lastHapticSecond = totalSeconds
            }
        }
    }

    private fun determineState(remaining: Long): TimerState {
        return when {
            remaining > ONE_MINUTE_MILLIS -> TimerState.WARNING_4_MIN
            remaining > TEN_SECONDS_MILLIS -> TimerState.PREP_1_MIN
            remaining > 0L -> TimerState.FINAL_COUNTDOWN
            else -> TimerState.STARTED
        }
    }

    fun reset() {
        timerJob?.cancel()
        _timerState.value = TimerState.IDLE
        _remainingMillis.value = 0
        _isHoldingFor4Min.value = false
        resetHapticTrackers()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
