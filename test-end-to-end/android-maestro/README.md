## Maestro end-to-end testing for Android

The end-to-end tests for the Android app are built using [Maestro](https://maestro.mobile.dev/) and
generate reports. The scripts have been written and tested on Linux. They may work on other systems,
but that has not been thoroughly tested.

### Prerequisites:

* Install Maestro as per the [official instructions](https://maestro.mobile.dev/getting-started/installing-maestro).
* Build the HTTP server from the [app-ktor-server](../../app-ktor-server/) module (e.g. ```./gradlew app-ktor-server:build```).
* Use Android SDK 33 emulator as the chrome browser **must** be updated otherwise H5P tests will fail. The version of Chrome that is
  included with SDK33 will work. To test on devices running earlier versions of Android:
    * Update Google Play Services (e.g. from [APKMirror](https://www.apkmirror.com/apk/google-inc/google-play-services/google-play-services-24-31-33-release/))
    * Update Chrome (e.g. from [APKMirror](https://www.apkmirror.com/apk/google-inc/chrome/chrome-127-0-6533-103-release/))
* Ensure the [app-android](../../app-android/) APK is installed on emulator/device
* Setup adb port forwarding to allow the emulator or device to connect to the 
  [test server controller](../../testserver-controller/) e.g. where testserver-controller runs on the
  default port (8075), then run:
```
adb reverse tcp:8075 tcp:8075
```
Note: ADB port forwarding on localhost is used instead of directly using the IP of the PC/server to 
ensure that the device and testcontroller can communicate, even if airplane mode is used during
testing.

* If running test(s) that require files from test-files, push the test-files to the download directory
  using the adb command:
```
adb push ../test-files/content/* /sdcard/Download/
```

### Running

Start the testserver-controller:
```
./gradlew testserver-controller:run --args='-P:mode=maestro'
```

Then use the Maestro command line to run tests:
```
maestro test e2e-tests/testname.yaml [-e TESTCONTROLLER_URL=http://localhost:8075] 
```

### Continuous integration run

Coming soon

### Resource IDs (testtags)

Editable fields: testTag should be the label, in snake_case e.g. if a field label is "First names",
then the id will be first_names . This applies to text fields, switches, drop downs.

Date/time combined fields: two test tags - one for date, one for time e.g.  dont_show_before_date,
dont_show_before_time

Buttons: testTag should be label_button

Rich text (click to move to edit): Most screens where the user can enter rich text, they will click
on the text and then be taken to another screen to actually edit the text (due to screen size
limitations).

ID to click on to move to next edit screen: com.toughra.ustadmobile:id/text_input_edit_text

ID to click on to actually edit rich text: com.toughra.ustadmobile:id/editor

**Other Ids**

Floating action button: floating_action_button (note: selecting this by the text in the button
does not work).

Action bar button (e.g. save/done): action_bar_button

Settings (action bar): settings_button

Accounts (top right): header_avatar

Title text: app_title

Exceptions:
Next/prev buttons on attendance edit: prev_day_button and next_day_button

Message list (chat) screen (no label): textfield: message_text

PersonEdit: phone number is split into country code dropdown ( tag: country_code ) and the in country
number ( tag: phone_number_text )
