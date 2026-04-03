# Bumps Countdown Timer

An Android application designed for rowing coxswains and officials to manage "Bumps" style race starts with precision and ease.

## Key Features

- **Scheduled Starts**: Set a division start time and let the app handle the countdown.
- **Dynamic Synchronization**: Sync to 4-minute or 1-minute guns instantly, even if you are slightly off-time.
- **Rolling Hold Pattern**: Handles race delays with a repeating 60-second hold that maintains perfect synchronization with the "intended" start time.
- **High-Visibility UI**: Color-coded screens for different race states (Warning, Prep, Countdown, Hold, Started).
- **Haptic Feedback**: High-intensity vibration patterns designed for outdoor use:
    - **500ms pulse** at every minute mark.
    - **100ms quick pulses** during the final 10 seconds.
    - **1500ms long pulse** at the start gun.

## How to Use

### 1. Schedule a Division Start
- Tap **SCHEDULE DIVISION START**.
- Select the time the division is scheduled to start.
- The app will automatically start a countdown to when the 4-minute gun is due.

### 2. Synchronization
- **4-MIN SYNC**: Use this if you hear the 4-minute gun and want to sync exactly to it.
- **1-MIN SYNC**: Use this if you hear the 1-minute gun and want to sync exactly to it.
- Otherwise, the app will guide you through the gun sequence if it's on time.

### 3. Rolling Hold
- If a division is delayed, tap **ROLLING HOLD**.
- This starts a repeating 60-second countdown.
- At the end of each minute, the app will ask: **"Did the [4|1] minute gun go?"**
    - **YES**: The app starts the corresponding countdown (4-min or 1-min), removing any latency from when you pressed the button.
    - **NO**: The app starts another 60-second hold, also removing latency to keep the hold perfectly synced to the minute marks.

### 4. Reset
- Tap **RESET** to clear the current timer and return to the idle state.

## Installation

The latest pre-built versions of the app are available on the **[GitHub Releases](https://github.com/triskaidekafeliks/BumpsCountdownTimer/releases)** page.

1.  Navigate to the Releases page.
2.  Download the latest `.apk` file (e.g., `BumpsCountdownTimer-v1.0-debug.apk`).
3.  On your Android device, open the downloaded file to install (you may need to allow "Installation from Unknown Sources" in your settings).

## Development

This project is built using:
- **Kotlin**
- **Jetpack Compose** for the modern UI.
- **Coroutines** for precise timing logic.
- **Material 3** design components.

### Building from Source

1. Clone the repository.
2. Open in Android Studio (Ladybug or newer).
3. Build using the `./gradlew assembleDebug` command or use the "Build APK" option in Android Studio.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
