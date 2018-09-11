# Ustad Mobile Android port

This is a normal gradle build and should open fine with Android studio. It includes the core and sharedse modules.

### Android Studio Setup

Gradle 4.6 (required to use annotationProcessor without plugins) does not allow __configure on
demand__. Open File - Settings - Build, Execution, Deployment, Compiler and uncheck configure on
demand if checked.

### To build

(run from root project directory):

> $ ./gradlew ':app-android:assembleDebug'

### To sign :

(run from root project directory):

> $ cp keystore.properties.example keystore.properties  <br/>
> $ # Edit keystore.properties as required using text editor <br/>
> $./gradlew ':app-android:assembleRelease'

### To test

#### Standalone tests

To run the main tests (that don't depend on a second device for testing peer-to-peer functionality,
it's a normal gradle task run. As the test app (but not the main app) with dependencies (e.g. Mockito)
exceeds the 64K method limit, a variant has been made with multidex enabled, which is used only
for testing and development purposes. The main app itself is well under the 64K method limit and uses
the vanilla and localconfig build variants.

 >$ ./gradlew connectedVanillamultidexDebugAndroidTest

#### Peer to peer networked tests

Network tests require bluetooth and wifi hardware with a running test slave server to 
communicate with. If bluetooth or wifi are disabled tests requiring that functionality 
will not run on Android.

To test network functionality two devices are required. 
One to run the tests and a second "slave" device that
is communicated with.

Set remote test slave server device serial in app-android/buildconfig.default.properties 
using the serial as listed by the adb devices command.


Start the remote test slave server device:
 >$./gradlew startTestSlaveServer

This automatically updates buildconfig.local.properties with the ip address, mac address, and
bluetooth mac of the device being used as the test slave server.

 Run the tests:
 >export ANDROID_SERIAL="TESTDEVICESERIAL" <br/>
 >
 >>$./gradlew connectedAndroidTest\

#### Testing on Windows bug error=206

If you see the error  ``` Createprocess error=206; the filename or extension is too long``` while running the Android Espresso tests it means that Gradle in Windows is unable to handle the long classpath. ie: this repo is a bit further down the folder structure. It seems that Gradle doesn't handle long command line on Windows yet. A solution to this error would be to move the UstadMobile repo closer to ```C:\```  eg: ```C:\repo\UstadMobile\```. You may be able to mount the repo on a virtual disk however we have not tested that yet.

### To use as a library in another app

Add Ustad Mobile's maven repo to your build.gradle file:

```
maven {
    url "http://devserver2.ustadmobile.com/repo-mvn"
}
```

Add the Ustad Mobile app library as a dependency to build.gradle:

```
compile "com.ustadmobile.app:app-android:0.2.4.65"
```

Open the first base point content listing:

```
startActivity(new Intent(this, BasePointActivity.class));
```

Login a particular user for content usage:
```
UstadMobileSystemImpl.getInstance().setActiverUser("username", context);
UstadMobileSystemImpl.getInstance().setActiveUserAuth("username", context);
```
