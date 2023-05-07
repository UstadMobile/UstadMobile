# Ustad Mobile

Ustad Mobile enables users to teach, learn, and share: online or offline. It is a learning
management system (LMS) built from the ground up to work with or without connectivity.

Ustad Mobile is licensed under the AGPLv3 license: please see the LICENSE file for details.

## Installing on a server (self-hosting)

See [INSTALL.md](INSTALL.md) for instructions for installation on your own server using binary 
downloads.

## Getting started building from source:

This is a Kotlin Multiplatform project. This repository contains the Android app, web app, and
backend server source code in its modules. 

* __Import the project in Android Studio__: Select File, New, Project from Version Control. Enter
https://github.com/UstadMobile/UstadMobile.git and wait for the project to import.

* __Build/run the server__: Run the server locally:

Linux/MacOS:
```
$ ./runserver.sh
```

Windows:
```
$ runserver.bat
```

This will start the server on port 8087. This will run the REST API which is required by the Android
and web apps. It will not include the web client app itself. To use the web client app in the browser,
you must build/run it (as below).

* __Build/run the Android and/or web client version__ : see [app-android](app-android/) for the
Android app, [app-react](app-react/) for the web app.

## Contributing

Contributions are welcome! If you're unsure about anything, please create an issue and label it as
a question.

* __Localization__ - Localization is done using on our [Weblate project](https://hosted.weblate.org/projects/ustad-mobile/).
 Please register on Weblate and then [create an issue](https://github.com/UstadMobile/UstadMobile/issues/new) 
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
* [core](core/) : Contains view models, ui state, core business logic.
* [sharedse](sharedse/): Contains some shared implementations for operating systems with a disk (JVM/Android)
* [app-ktor-server](app-ktor-server/): Contains the HTTP rest server (implemented using KTOR)
* [app-android](app-android/): Contains Android implementation, forms the basis of the app in
  app-android-launcher but can also be used as a library in other apps.
* [app-react](app-react/): Contains the web version implemented using Kotlin/JS
* [app-android-launcher/](app-android-launcher/): Standalone app launcher that uses app-android.
* [lib-database](lib-database/): contains the database, DAO, and entity classes.
* [lib-util](lib-util/): Small utility functions
* [test-end-to-end](test-end-to-end/) End-to-end tests that run the app and server.
* [testserver-controller](testserver-controller/) An HTTP server that can control starting and 
  stopping the main server, and manage adb screen recording. Used by end-to-end testing.

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


