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
### Login and signup with passkey
If use server with https(eg. https://ustad.example.com) then need to Add support for Digital Asset
Links, need to generate assetlinks.json file. You can generate it from android studio from tools>
app links assistant> create applink > open url mapping editor > from + option can add url then from
Digital asset links file generator can generate that assetlinks.json file.
You need to copy that json file and that json should return from this address eg.
https://ustad.example.com/.well-known/assetlinks.json

If using http server (local IP address) then you need to add this
<intent-filter android:autoVerify="true">
<action android:name="android.intent.action.VIEW" />
<category android:name="android.intent.category.DEFAULT" />
<category android:name="android.intent.category.BROWSABLE" />
<data android:scheme="http" />
<data
android:host="YOUR IP ADDRESS"
android:port="8087" />
</intent-filter>

inside AppActivity intent filter , replace host with your ip address. After that if your device is
below android 12 then use this command adb shell am compat enable 175408749 PACKAGE_NAME
to support web address and above 12 you can manually verify from mobile in app info> set as default>
supported web addresses > switch toggle on.

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
