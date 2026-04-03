# Bumps Countdown Timer

An Android application designed for rowing coxswains and officials to manage "Bumps" style race starts with precision and ease.

## Key Features

- **Scheduled Starts**: Set a division start time and let the app handle the countdown.
- **Dynamic Synchronization**: Sync to 4-minute or 1-minute guns instantly, even if you are slightly off-time.
- **Rolling Hold Pattern**: Handles race delays with a repeating 60-second hold that maintains perfect synchronization with the "intended" start time.
- **Smart Latency Removal**: When confirming a gun after a delay, the app automatically removes the reaction time latency from the next timer.
- **High-Visibility UI**: Color-coded screens for different race states (Warning, Prep, Countdown, Hold, Started).
- **Haptic Feedback**: Stay informed without looking at the screen via distinct vibrations for minute marks, the final 10 seconds, and the start gun.

## Installation

You can find the latest pre-built version of the app in the [Releases](./Releases) folder.

1.  Navigate to the `Releases` folder in this repository.
2.  Download the latest `.apk` file (e.g., `BumpsCountdownTimer-v1.0-debug.apk`).
3.  On your Android device, open the downloaded file to install (you may need to allow "Installation from Unknown Sources" in your settings).

For detailed usage instructions, see the [Releases README](./Releases/README.md).

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
