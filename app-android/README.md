# Ustad Mobile Android port

This is a normal gradle build and should open fine with Android studio. It includes the core and sharedse modules.

### To build

(run from root project directory):

> $ ./gradlew ':app-android:assembleDebug'

### To sign :

(run from root project directory):

> $ cp keystore.properties.example keystore.properties  <br/>
> $ # Edit keystore.properties as required using text editor <br/>
> $./gradlew ':app-android:assembleRelease'

### To test

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
 >$./gradlew connectedAndroidTest
 
 
