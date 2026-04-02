package com.example.bumpscountdowntimer.ui.timer

enum class TimerState {
    IDLE,
    PRE_SEQUENCE,       // Counting down to the 4-minute warning gun
    WARNING_4_MIN,      // 4:00 to 1:00
    PREP_1_MIN,         // 1:00 to 0:10
    FINAL_COUNTDOWN,    // 0:10 to 0:00
    ROLLING_HOLD,       // Repeating 60s countdown for delays
    STARTED             // t > 0 post-start
}
