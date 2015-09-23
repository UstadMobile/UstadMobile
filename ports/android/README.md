Ustad Mobile for Android


The Android version uses an ant task to copy the core source into
it's directory.  This is to aid debugging: Android studio does not
respect breakpoints in JARs even when the sources are known to it.

Packages are therefor split up as follows:
com.ustadmobile.core : Everything here except for 
com.ustadmobile.impl.UstadMobileSystemImplFactory and
com.ustadmobile.view.ViewFactory will be copied from the core
directory (../../core).  Any other files there will be deleted
when the build process runs.

Test packages are split up the same way.  In the testing dir
anything under ustadmobile.test.core will be deleted and 
replaced by the preprocessed files from the core directory.

Update core library

./updatecore

Run tests:
./gradlew connectedAndroidTest

Build Debug Version:
./gradlew assembleDebug

Build Release Version:
./gradlew assembleRelease

Watching logcat/Debugging:

The logtag is UMLogAndroid
$ adb logcat -s 'ActivityManager:I UMLogAndroid:*'
