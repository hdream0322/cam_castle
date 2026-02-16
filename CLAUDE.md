# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**camsung** is an Android app that silences camera shutter sounds on Samsung phones by modifying the system setting `csc_pref_camera_forced_shuttersound_key`. As of version 1.3.0, the app targets API level 36 to enable installation on Android 14+ devices without ADB bypass requirements.

## Build & Development Commands

### Building
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Clean build
./gradlew clean build
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run tests via fastlane
bundle exec fastlane test
```

### Fastlane Automation
```bash
# Install dependencies
bundle install

# Take automated screenshots
bundle exec fastlane screenshots

# Build release APK
bundle exec fastlane beta
```

### Installation
Standard APK sideloading works on all Android 6.0+ devices:
```bash
adb install app/build/outputs/apk/release/app-release.apk
```

**Note:** Versions prior to 1.3.0 require the `--bypass-low-target-sdk-block` flag on Android 14+.

## Architecture

### Core Components

**MainActivity** ([MainActivity.kt](app/src/main/java/android/com/ericswpark/camsung/MainActivity.kt))
- Main UI with toggle switch and boot lock button
- Handles app shortcuts (app://mute, app://unmute) for automation
- Manages WRITE_SETTINGS permission requests
- Optional camera auto-launch after toggling

**CameraHelper** ([CameraHelper.kt](app/src/main/java/android/com/ericswpark/camsung/CameraHelper.kt))
- Core logic for muting/unmuting camera shutter
- Modifies system setting: `csc_pref_camera_forced_shuttersound_key`
- Value 0 = muted, 1 = unmuted
- Requires WRITE_SETTINGS permission

**Receiver** ([Receiver.kt](app/src/main/java/android/com/ericswpark/camsung/Receiver.kt))
- BroadcastReceiver for BOOT_COMPLETED events
- Handles Tasker automation via ACTION_SET_CAMERA broadcast
- Intent data URIs: app://mute, app://unmute

**FAQActivity & Components**
- FAQ system with adapter pattern
- Located in `faq/` package

### Permission Model

The app requires `android.permission.WRITE_SETTINGS` (SDK 23+) to modify system settings. Users must grant this permission through Android's settings management screen.

### Automation Integration

Two automation methods are supported:

1. **App Shortcuts**: Deep links (app://mute, app://unmute) handled by MainActivity
2. **Tasker Integration**: Broadcast receiver accepts ACTION_SET_CAMERA intents with data URIs
   - See [tasker/](tasker/) folder for example Tasker profiles

## Critical Constraints

### targetSdkVersion

As of version 1.3.0, the app targets SDK 36 to enable installation on Android 14+ devices without requiring ADB bypass commands.

```gradle
targetSdkVersion 36
```

The WRITE_SETTINGS permission is handled correctly for API 23+ using `Settings.System.canWrite()` and the `Settings.ACTION_MANAGE_WRITE_SETTINGS` intent. This pattern works identically across all SDK versions from 23 to 36.

**Historical Context:** Prior to v1.3.0, the app targeted SDK 22 due to concerns about WRITE_SETTINGS access. This restriction has been removed after verifying that the modern permission flow (already implemented) works correctly with SDK 36.

### Java Version

The project uses JDK 21. Ensure your environment is configured accordingly:
- sourceCompatibility: JavaVersion.VERSION_21
- targetCompatibility: JavaVersion.VERSION_21
- Kotlin jvmToolchain: 21

### Installation Requirements

- **Android 13 and below:** Standard APK sideloading works
- **Android 14 and above:** Direct APK installation (no ADB bypass required as of v1.3.0)

## Build Configuration

### Gradle Structure
- Root [build.gradle](build.gradle): Project-level configuration with Kotlin 2.2.0
- [app/build.gradle](app/build.gradle): App module configuration
- compileSdkVersion: 36
- minSdkVersion: 22
- ProGuard enabled for release builds

### Dependencies
- Kotlin stdlib
- AndroidX (core-ktx, appcompat, material)
- ConstraintLayout
- Preference library (for SettingsActivity)
- Fastlane screengrab for automated screenshots

## Testing Strategy

- Unit tests: [app/src/test/](app/src/test/)
- Instrumented tests: [app/src/androidTest/](app/src/androidTest/)
- Screenshot automation via Fastlane's screengrab tool

## Package Structure

```
android.com.ericswpark.camsung
├── MainActivity           # Main UI and business logic
├── CameraHelper          # System settings manipulation
├── Receiver              # Boot and automation receiver
├── SettingsActivity      # App preferences
└── faq/                  # FAQ feature
    ├── FAQ               # Data model
    ├── FAQAdapter        # RecyclerView adapter
    └── FAQActivity       # FAQ display
```
