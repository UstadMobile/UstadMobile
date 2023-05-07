# Ustad Mobile Android launcher module

This module is normal Android application module. The app-android module is an Android library which
can (if desired) be used as a library in other apps. It can be built/run using the latest Android
studio or on the [command line](https://developer.android.com/build/building-cmdline).

### Command line debug APK build 

(run from root project directory):

Linux/MacOS
```
$ ./gradlew app-android-launcher:assembleDebug
```
Windows:
```
$ gradlew app-android-launcher:assembleDebug
```

### Command line build signing :

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
