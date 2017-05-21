# Ustad Mobile Android port

This is a normal gradle build and should open fine with Android studio. It includes the core and sharedse modules.

###To build

(run from root project directory):

> $ ./gradlew ':app-android:assembleDebug'

###To sign :

(run from root project directory):

> $ cp keystore.properties.example keystore.properties  <br/>
> $ # Edit keystore.properties as required using text editor <br/>
> $./gradlew ':app-android:assembleRelease'

### To test

To test network functionality two devices are required. 
One to run the tests and a second "slave" device that
is communicated with.

Set remote test slave server device serial, port, bluetooth address and IP address as indicated below:-
* app-android/buildconfig.default.properties (device serial)
* buildconfig.default.properties (port,bluetooth address,IP address)<br/>

Start the remote test slave server device:
 >$./gradlew startTestSlaveServer
 
 Run the tests:
 >export ANDROID_SERIAL="TESTDEVICESERIAL" <br/>
 >$./gradlew connectedAndroidTest
 
 