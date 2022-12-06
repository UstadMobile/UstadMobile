## Maestro end-to-end testing for Android

The end-to-end tests for the Android app are built using [Maestro](https://maestro.mobile.dev/)

### Prerequisites:

* Install Maestro as per the [official instructions](https://maestro.mobile.dev/getting-started/installing-maestro).
* Build the android release apk from the [app-android-launcher](../app-android-launcher/) module and the
  HTTP server from the [app-ktor-server](../app-ktor-server/) module. This would be done by the normal
  **gradlew build** command.
* Start an Android emulator or connect a physical device and get the serial of the device (e.g. using the **adb devices** command)

### Running:

```
$ ./run-maestro-test.sh --serial1 emulator-5554
```
Where emulator-5554 is the serial of the emulator as per the **adb devices** command.

Options:

* **--test** specify a test to run as per the test flows found in e2e-tests e.g. --test 001_001_admin_can_add_content_001-h5p
* **--apk** specify a particular apk file to test and install. By default the test runner expects to
 use the release APK file built from source
* **--console-output** use Maestro console output instead of saving to a JUnit XML. Helpful to see 
more verbose output when designing/running/debugging flows etc.
