# app-android

This module provides the app for Android. Fragments, activities, services, etc. are here. This is 
single activity app that uses [Jetpack navigation](https://developer.android.com/guide/navigation) 
via fragments.

## Getting started from source

* Follow steps in the [main project README](../README.md#development-environment-setup) to setup development
  environment and start the server. _Android SDK command line tools are required_:
  In Android Studio: Menu - Tools - SDK Manager - SDK Tools Tab - Check
  Android SDK Command Line Tools (Latest). This is required by the [Android Emulator Gradle Plugin](https://github.com/quittle/gradle-android-emulator)
  which is used to start/stop the emulator to create the [baseline profile](https://developer.android.com/topic/performance/baselineprofiles/overview).

* ![android run screenshot](android-run.png)  
Run the app by selecting "app-android" in the Android toolbar and clicking the run button.
* Enter the URL of the server and tap "next". You can use the local IP address of your laptop. e.g.
__http://192.168.1.42:8087/__ where your IP address is 192.168.1.42 (server default port is 8087) . 
You __cannot__ use 127.0.0.1 or localhost (on the Android device/emulator, this refers to the 
device/emulator, not the host PC.). If using an emulator, you can use http://10.0.2.2:8087/ (because
10.0.2.2 on an Android emulator always refers to the host PC).
* Enter the username admin, and the admin password. See [main project README](../README.md#development-environment-setup)
for how to find the initial admin password.

### Command line debug APK build

(run from root project directory):

Linux/MacOS
```
$ ./gradlew app-android:assembleDebug
```
Windows:
```
$ gradlew app-android:assembleDebug
```

### Digital asset links

Verified digital assets links [e.g. app links](https://developer.android.com/training/app-links/verify-android-applinks) 
are required for deep links and passkeys. The SHA-256 signature on Google Play Console can be found 
under Test and Release, Setup, App Signing.

The [official docs](https://developer.android.com/training/app-links#manage-verify)
cover verifying links for the version delivered through Google Play (e.g. using Google Play signing keys).

__When using a test/debug version__:
* Build the apk (e.g. ```./gradlew app-android:assembleDebug```)
* Get the SHA256
```
~/Android/Sdk/build-tools/33.0.1/apksigner verify --print-certs ./build/outputs/apk/debug/app-android-debug.apk
```
* Convert the generated SHA-256 into : separated version
```
echo (sha256 from apksigner verify) | sed 's/../&:/g; s/:$//' | tr [:lower:] [:upper:]
```
* Add the SHA-256 to [assetlinks.json] and publish assetlinks.json in .well-known on https for domain.
  Note: the SHA-256 in the default assetlinks.json is the Google Play signing key for the Ustad Mobile
  app. The assetlinks.json file can be checked using [Google's statement list tester](https://developers.google.com/digital-asset-links/tools/generator).
  Google APIs can cache the statements; current status can be checked using the 
  [Digital Asset Links API](https://developers.google.com/digital-asset-links/reference/rest) e.g.
  ```https://digitalassetlinks.googleapis.com/v1/statements:list?source.web.site=https://ustadmobile.app&relation=delegate_permission/common.handle_all_urls```

__Setting or adding a domain for app links and passkeys:__

* Set systemUrl in buildconfig properties (see KDoc on [SystemUrlConfig.kt](../core/src/commonMain/kotlin/com/ustadmobile/core/impl/config/SystemUrlConfig.kt))
* Add the domain to [AndroidManifest.xml](./src/main/AndroidManifest.xml) app link intent-filter
* Publish [assetlinks.json] as per [official docs](https://developer.android.com/training/app-links#manage-verify) including SHA256 fingerprints
* Add domain to [assets_statement_values.xml](app-android/src/main/res/values/assets_statement_values.xml)

### Command line signing (for release APK) :

This is required when you want to upload the app to Google Play or other app stores. This works
as per [App Signing Documentation](https://developer.android.com/studio/publish/app-signing).

**Option 1: Create a keystore properties file in project root directory.**

Linux/MacOS
```
$ cp keystore.properties.example keystore.properties
```
Windows:
```
$ COPY keystore.properties.example keystore.properties
```

Edit keystore.properties to specify your signing key (see
[Remove signing information from your build files](https://developer.android.com/studio/publish/app-signing#secure-shared-keystore)
official documentation if needed for extra info on properties).

**Option 2: Specify the keystore properties location using an environment variable**

Set the KEYSTORE environment variable before running the gradle build command

Windows:
```
SET KEYSTORE=C:\PATH\TO\keystore.properties
```
Linux/MacOS:
```
export KEYSTORE=/path/to/keystore.properties
```

The release variant will now be signed with the key as per keystore.properties.


(run from root project directory):

```
 $ ./gradlew ':app-android:assembleRelease'
```
Make sure you are running an SDK33+ device or emulator connected using ADB for the
[baseline profile](https://developer.android.com/topic/performance/baselineprofiles/overview) build.

### Known issues
* Collapsing toolbar / coordinator layout does not work with Jetpack compose views: see https://developer.android.com/reference/kotlin/androidx/compose/ui/input/nestedscroll/package-summary 
and https://issuetracker.google.com/issues/174348612
