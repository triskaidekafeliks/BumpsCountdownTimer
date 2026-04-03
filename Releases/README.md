# Releases

This folder contains the latest build of the Bumps Countdown Timer app.

## Latest Version
- **File**: `BumpsCountdownTimer-v1.0-debug.apk`

## Installation Instructions

1.  **Download the APK**: Download the `BumpsCountdownTimer-v1.0-debug.apk` file from this folder to your Android device.
2.  **Enable Unknown Sources**: If you haven't done this before, you may need to allow your browser or file manager to "Install unknown apps" in your device's security settings.
3.  **Install**: Open the downloaded `.apk` file and tap **Install**.
4.  **Launch**: Once installed, find the **Bumps Countdown** app in your app drawer and open it.

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

## Features
- **Haptic Feedback**:
    - Long pulse at every minute mark.
    - Short ticks during the final 10 seconds.
    - Distinct pulse at the start.
- **Color Coding**:
    - **Blue**: 4-minute warning sequence.
    - **Yellow**: 1-minute prep sequence.
    - **Red**: Final 10-second countdown.
    - **Orange**: Rolling hold (delay) state.
    - **Green**: Race started.
