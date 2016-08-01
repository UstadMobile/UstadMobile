Ustad Mobile for Android

Gradle is used to include the core package of controllers
(shared even with J2ME) and sharedse which contains some
system methods for "standard" edition platforms.

Packages are therefor split up as follows:
com.ustadmobile.core : Core logic shared with J2ME
com.ustadmobile.port.sharedse : Logic shared amongst "Full fat" SE platforms (Android, J2ObjC target, J2SE)
com.ustadmobile.port.android : Android specific implementation

Run tests:

Normally Android test runs are done using ./gradlew connectedAndroidTest .
We however have tests that require the test server to run which
simulates broken connections etc.  Tests must be run on an emulator
or a connected Android device which is on the same network as the 
host from which runtests is run.

To run tests:
./runtests

To update test config (set network addresses to local IP address):
./runtests updateonly


Build Debug Version:
./gradlew assembleDebug

Build Release Version:
./gradlew assembleRelease

Watching logcat/Debugging:

The logtag is UMLogAndroid
$ adb logcat -s 'ActivityManager:I UMLogAndroid:* TestRunner:*'
