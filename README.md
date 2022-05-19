# Ustad Mobile

Ustad Mobile enables learners to access and share content offline. It uses peer-to-peer offline
networking (using Bluetooth Low Energy and WiFi Direct) to enable users to share content directly
without any additional hardware.

Supported content formats:
* [EPUB content](http://idpf.org/epub): Anything you can do with HTML5 can be in EPUB (video,
   interactive quizzes, etc).
* [Experience API](http://www.tincanapi.com): TinCan API Zip file (containing tincan.xml)
* [H5P](https://www.h5p.org): H5P interactive content
* MP4, WEBM video

Ustad Mobile is licensed under the AGPLv3 license: please see the LICENSE file for details.

## Installing on a server (self-hosting)

See [app-ktor-server](app-ktor-server/) for instructions on building the server JAR. Then see
[INSTALL.md](INSTALL.md) for instructions on running the server.

## Getting started building from source:

* __Install latest Node-14x__ as per [Instructions on Node website](https://nodejs.org/en/download/).
  Kotlin/JS depends on it to build

* __Import the project in Android Studio__: Select File, New, Project from Version Control. Enter
https://github.com/UstadMobile/UstadMobile.git and wait for the project to import.

* __Build it__ :
Linux command line:
```
./gradlew -Pktorbundleproductionjs=true build
```
Windows command line:
```
gradlew  -Pktorbundleproductionjs=true build
```

If you see compilation errors regarding MessageID, this seems to be caused by a gradle dependency
ordering issue. Run:
```
gradlew core:prepareLocale
```
Then try again (Sorry).

Start the server:

You should now be able to open the web version on http://localhost:8087/ . The admin password will
be found in ./app-ktor-server/data/singleton/admin.txt

* __Install and run the Android app__ : run
```
adb install ./app-android-launcher/build/outputs/apk/debug/app-android-launcher-debug.apk
```

You should now be able to open the Android app. When the Android app starts, enter the address of
the rest server
e.g. http://192.168.1.123:8087/ where 192.168.1.123 is the IP address of your development machine.

## Contributing

Contributions are welcome! If you're unsure about anything, please create an issue and label it as
a question.

* __Localization__ - Localization is powered by our Weblate server at [https://weblate.ustadmobile.com](https://weblate.ustadmobile.com).
 Please register on the weblate server [create an issue](https://github.com/UstadMobile/UstadMobile/issues/new) 
 with the subject "Localization - language name" and let us know what language you would like to 
 translate into. We can then give you permission to start translating!
* __Feature requests__ - Let us know what features you would like to see. [Create an issue](https://github.com/UstadMobile/UstadMobile/issues/new)
 and label it as a feature request.
 * __Bug reports__ - this project is written primarily in Kotlin as a Kotlin Multiplatform. You can 
 [create an issue](https://github.com/UstadMobile/UstadMobile/issues/new) or even send us a pull request.
 * __Code contributions__ - we welcome pull requests. Please ensure your contribution is readable,
 follows existing patterns and provides the required tests.

### Code structure

This multi-module Gradle project built using Kotlin Multiplatform. It builds for:

* Android (app-android)
* KTOR Server (JVM) (app-ktor-server)

Code is contained (mostly) in the following modules:
* [core](core/) : Contains presenters, view interfaces, and core business logic.
* [sharedse](sharedse/): Contains some shared implementations for operating systems with a disk (JVM/Android)
* [app-ktor-server](app-ktor-server/): Contains the HTTP rest server (implemented using KTOR)
* [app-android](app-android/): Contains Android implementation, forms the basis of the app in
  app-android-launcher but can also be used as a library in other apps.
* [app-react](app-react/): Contains the web version implemented using Kotlin/JS
* [app-android-launcher/](app-android-launcher/): Standalone app launcher that uses app-android.
* [lib-database](lib-database/): core Database and Data Access Object (DAO) classes. See this
module's README for further details on the relationship between different database modules.
* [lib-database-entities](lib-database-entities): Database entity objects
* [lib-database-android](lib-database-android/) Android database
implementation (a Room Persistence Framework database generated from lib-database).
* [lib-util](lib-util/): Small utility functions

To build debug / release versions for any given platform please see the README in the directory for that platform.

## Configuration management

There are two parts to the configuration:

* Build configuration : buildconfig.properties contains properties used
during the build and testing process.

* App config : a .properties file that is contained in the assets. This can be overriden when the
  app is used as a library. By default [com/ustadmobile/core/appconfig.properties](core/src/main/assets/com/ustadmobile/core/appconfig.properties) 
  will be used. This can be overriden by setting the com.ustadmobile.core.appconfig manifest property 
  (e.g. meta-data in AndroidManifest.xml).

The build configuration system uses two .properties files: 
* **buildconfig.default.properties**: Contains default options
* **buildconfig.local.properties** : Contains any custom options and is excluded from git. Any option set in buildconfig.local.properties will override what's set in the default properties.


