# Ustad Mobile Android port

This is a normal gradle build and should open fine with Android studio. It includes the core and
sharedse modules.

### To build

(run from root project directory):

```
 $ ./gradlew ':app-android:assembleDebug'
```

### To sign :

(run from root project directory):

```
$ cp keystore.properties.example keystore.properties  <br/>
$ # Edit keystore.properties as required using text editor <br/>
$./gradlew ':app-android:assembleRelease'
```


### Build flavors

There are three flavors in the project:

* Vanilla: the base flavor. Supports SDK18+ and does not use multidex (the app on it's own does not
exceed 64K methods).
* DevMinApi21: SDK21+ only. This is recommended for development purposes - tests can be run without
the need to multidex.
* VanillaMultidex: SDK18+ and uses multidex. This is used to run tests on Android devices below SDK21.


### Tests

 >$ ./gradlew connectedVanillamultidexDebugAndroidTest
 
Note: Espresso end-to-end tests are being updated. 

### To use as a library in another app

This is a work in progress and has not been tested recently. The consuming app can set a manifest
preference "com.ustadmobile.core.appconfig" to set the path to the appconfig to override the
defaults.

Add Ustad Mobile's maven repo to your build.gradle file:

```
maven {
    url "http://devserver2.ustadmobile.com/repo-mvn"
}
```

Add the Ustad Mobile app library as a dependency to build.gradle:

### Known issues

* InputMethodManager memory leak on debug variant: known issue caused by pre-v10 Android
  [as per this report](https://github.com/square/leakcanary/issues/256).
