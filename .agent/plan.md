# Project Plan

Bumps Countdown Timer (BumpsRaceTimer) is a specialized rowing start sequence timer. It handles a 4-minute to 1-minute to start countdown with sync capabilities and a 'Rolling Hold' mode for delays. The UI must be high-visibility with state-based colors and massive typography. It must use Kotlin, Compose, M3, and Haptic Feedback.

## Project Brief

# Bumps Countdown Timer - Project Brief

## Features
- **Standard Bumps Start Sequence**: Automatically manages the countdown from the 4-minute warning to the 1-minute warning, culminating in the start signal.
- **Precision Sync Buttons**: One-tap "4-min Sync" and "1-min Sync" buttons to immediately align the app's timer with the official race official's cannons.
- **Intelligent Rolling Hold**: A specialized mode for delayed starts that loops a 60-second countdown with full-screen prompts and manual "1-Min Gun Now" overrides.
- **High-Visibility State System**: Utilizes a massive "DisplayLarge" timer and dynamic background colors (Blue, Yellow, Orange, Red, Green) for instant status recognition in high-pressure environments.
- **Haptic Pulse Feedback**: Tactile vibration alerts at every 60-second mark and a per-second haptic pulse during the critical final 10-second countdown.

## High-Level Technical Stack
- **Kotlin**: Primary programming language for robust logic and concurrency.
- **Jetpack Compose**: Modern declarative UI framework for the adaptive, high-visibility interface.
- **Material 3**: Implementation of the latest Material Design system for a vibrant, energetic aesthetic.
- **Android ViewModel & StateFlow**: Ensures timer state persistence across configuration changes (rotations) and handles reactive UI updates.
- **System Clock Integration**: Uses `System.currentTimeMillis()` as the underlying source of truth to ensure zero clock drift during the sequence.

## Implementation Steps
**Total Duration:** 52h 44m 29s

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

