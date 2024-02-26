# app-android-baselineprofile

This module provides a baseline profile and startup profile to improve performance on Android as per
https://developer.android.com/topic/performance/baselineprofiles/overview

To run:
1. Start an Android SDK28+ Emulator
2. Run Gradle task:
```
./gradlew app-android:generateReleaseBaselineProfile
```
