# Project Plan

Add a 'Scheduled Division Start' feature.
- At startup or after a sequence, allow setting a division start time.
- Count down to the 4-minute gun based on this time.
- If the 4-minute gun is delayed (doesn't fire at the scheduled time), enter a 1-minute 'Rolling Hold' pattern similar to the existing one for the 1-minute gun.
- Maintain existing sync and manual override capabilities.

## Project Brief

# Bumps Countdown Timer - Project Brief

## Features
- **Scheduled Division Start**: Allows users to set a specific start time for a race division, automatically counting down to the initial 4-minute warning gun.
- **Automated Start Sequence**: Orchestrates the transition from 4-minute (Blue) to 1-minute (Yellow) to the Start (Green) signals, ensuring the coxswain or official stays on track.
- **Precision Sync & Overrides**: Includes high-latency "4-min Sync" and "1-min Sync" buttons to instantly align the app's internal clock with the official race cannons.
- **Adaptive Rolling Hold**: Manages delays with a 60-second looping "Rolling Hold" (Orange) if the 4-minute or 1-minute guns are late, featuring full-screen "Did the cannon fire?" prompts and manual overrides.
- **High-Visibility & Tactile UI**: Uses massive typography and a state-based color system for instant recognition in high-glare environments, supplemented by per-second haptic pulses during the final 10-second countdown.


## High-Level Technical Stack
- **Kotlin**: The primary language for modern, concise, and safe Android development.
- **Jetpack Compose**: Used for building the high-visibility, reactive user interface.
- **Material 3**: Implementation of the latest Android design system for a vibrant and accessible UI.
- **ViewModel & StateFlow**: Ensures the countdown state is preserved across configuration changes and provides a reactive stream of data to the UI.
- **Kotlin Coroutines**: Manages the precise timing loops and background processing for the countdown logic.
- **KSP (Kotlin Symbol Processing)**: Used for efficient code generation and dependency processing.
- **System Clock Integration**: Utilizes `System.currentTimeMillis()` as the absolute source of truth to prevent clock drift during the multi-minute sequence.

## Implementation Steps
**Total Duration:** 73h 23s

### Task_1_Core_Timer_Logic: Implement the core timer engine in a ViewModel using StateFlow and System.currentTimeMillis(). This includes the standard 4-minute to 1-minute countdown logic, state management for different phases (Warning, Prep, Final, Start), and the sync button functionality (4-min and 1-min sync).
- **Status:** COMPLETED
- **Updates:** User requested to stop the build process while keeping the generated files. Task 1 (Core Timer Logic) is being marked as completed per the current state of the files, acknowledging that a potential infinite loop in tests was reported by the user.
- **Acceptance Criteria:**
  - TimerViewModel accurately calculates remaining time
  - Sync buttons correctly reset the timer to 4:00 and 1:00
  - Timer states are correctly defined and broadcast
- **Duration:** 11h 49m 52s

### Task_2_High_Visibility_UI: Develop the main user interface using Jetpack Compose with massive DisplayLarge typography. Implement dynamic background color changes based on the timer state (e.g., Blue for warning, Yellow for prep, Red for final countdown, Green for start). Ensure the UI is clean and high-visibility.
- **Status:** COMPLETED
- **Updates:** User requested to stop the test loop and will verify the 'Rolling Hold' logic manually. The UI implementation including massive typography, dynamic background colors, and the Rolling Hold overlay is completed and saved.
- **Acceptance Criteria:**
  - Massive typography for the timer display
  - Background colors update automatically based on timer state
  - UI is responsive to state changes from the ViewModel
  - Edge-to-edge display is enabled
- **Duration:** 10h 3m 19s

### Task_3_Rolling_Hold_Haptics: Implement the 'Rolling Hold' mode which loops a 60-second countdown and includes a manual '1-Min Gun Now' override. Integrate haptic feedback pulses for the 60-second marks and per-second pulses during the final 10 seconds of any countdown.
- **Status:** COMPLETED
- **Updates:** Implemented haptic feedback for 60-second marks and per-second pulses during the final 10-second countdown. Refined the Rolling Hold logic to loop every 60 seconds with a 'Did the cannon fire?' prompt and added a '1-Min Gun Now' override. Addressed the reported test hang by optimizing the coroutine tick loop and ensuring tests use virtual time correctly. Verified the fix with unit tests in TimerViewModelTest.kt.
- **Acceptance Criteria:**
  - Rolling Hold mode loops correctly every 60 seconds
  - '1-Min Gun Now' override forces transition to the final 1-minute sequence
  - Haptic pulses occur at 60s intervals
  - Per-second haptic feedback triggers during the final 10 seconds
- **Duration:** 10h 43m 55s

### Task_4_Branding_Theme: Apply a vibrant Material 3 color scheme and theme (light and dark modes). Create and implement an adaptive app icon that reflects the rowing/timer purpose of the application.
- **Status:** COMPLETED
- **Updates:** Applied a vibrant Material 3 theme with support for light/dark modes and dynamic coloring. Created and implemented a custom adaptive app icon featuring a stopwatch and rowing oar. Refined the UI with massive typography (120sp) and polished Material 3 components for optimal visibility and usability in high-pressure rowing start environments. Verified the build with assembleDebug.
- **Acceptance Criteria:**
  - Material 3 theme with vibrant colors applied
  - Adaptive app icon is correctly configured and visible on the launcher
  - UI follows Material 3 guidelines and energetic aesthetic
- **Duration:** 10h 3m 56s

### Task_5_Final_Verification: Perform a final build and comprehensive run-through of the application. Verify all countdown sequences, sync features, and rolling hold modes. Check for stability and UI consistency.
- **Status:** COMPLETED
- **Updates:** Final verification completed. All core features (Sync, Rolling Hold, Haptics, High-Visibility UI) are implemented and verified. Unit tests in TimerViewModelTest.kt pass, ensuring stability and correct state transitions. The build (assembleDebug) is successful. The app features a vibrant Material 3 theme and a custom adaptive app icon. Critical UI requirements (120sp typography, state-based background colors) are met. Edge-to-edge display is enabled. All reported issues (infinite loops in tests) have been resolved.
- **Acceptance Criteria:**
  - Project builds successfully
  - App does not crash during any sequence
  - All features (Sync, Rolling Hold, Haptics) work as expected
  - Critic agent verifies application stability and alignment with requirements
- **Duration:** 10h 3m 27s

### Task_6_Scheduled_Start_Logic: Enhance TimerViewModel to support a scheduled division start time. Implement the countdown logic leading up to the 4-minute warning and a new 'Rolling Hold' state specifically for delays of the 4-minute gun. Ensure existing sync and override functionalities remain intact.
- **Status:** COMPLETED
- **Updates:** Enhanced TimerViewModel to support a scheduled division start time. Implement the countdown logic leading up to the 4-minute warning and a new 'Rolling Hold' state specifically for delays of the 4-minute gun. Ensure existing sync and override functionalities remain intact. Added PRE_SEQUENCE state and setScheduledStartTime method. Updated Rolling Hold logic to adapt based on whether it's waiting for the 4-min or 1-min gun. Verified with unit tests in TimerViewModelTest.kt.
- **Acceptance Criteria:**
  - ViewModel correctly calculates time until the 4-minute gun
  - 4-minute Rolling Hold loops every 60 seconds if not synced
  - Manual sync overrides work correctly from the pre-sequence state
- **Duration:** 10h 4m 19s

### Task_7_Scheduled_UI_and_Verification: Update the UI to include a mechanism for setting the scheduled division start time. Add high-visibility states for the pre-sequence countdown and the 4-minute Rolling Hold prompts. Conduct a final build and verification to ensure stability and requirement alignment.
- **Status:** COMPLETED
- **Updates:** Fixed bugs related to the 'Schedule Start' button visibility and Rolling Hold prompt labels. Updated TimerScreen.kt to make the 'Schedule Start' button the primary action in IDLE state. Refined TimerViewModel.kt to intelligently set the isHoldingFor4Min flag when starting a Rolling Hold from IDLE or PRE_SEQUENCE. Improved state labels and button casing for a more professional, high-visibility aesthetic. verified with unit tests and previews.
- **Acceptance Criteria:**
  - UI allows setting or editing the division start time
  - Pre-sequence and 4-min Rolling Hold states are visually distinct
  - App builds successfully and passes all sequence tests without crashing
- **Duration:** 10h 11m 35s

