# Ustad Mobile Android app module

This is a normal gradle build and should build and run with Android Studio as normal. It can normally
be selected from the list of run items at the top of Android Studio to run on the emulator as 
normal.

### Command line debug APK build 

(run from root project directory):

```
 $ ./gradlew :app-android-launcher:assembleDebug
```
(If running on Windows, do not use ./ before the gradlew command)

### Command line release APK build :

You need to specify the signing key and password to use

(run from root project directory):

```
$ cp keystore.properties.example keystore.properties  <br/>
$ # Edit keystore.properties as required using text editor <br/>
$./gradlew ':app-android-launcher:assembleRelease'
```
