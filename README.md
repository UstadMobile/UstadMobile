# Ustad Mobile

Ustad Mobile enables users to teach, learn, and share: online or offline. It is a learning
management system (LMS) built from the ground up to work with or without connectivity.

Ustad Mobile is licensed under the AGPLv3 license: please see the LICENSE file for details.

## Installing on a server (self-hosting)

See [INSTALL.md](INSTALL.md) for instructions for installation on your own server using binary 
downloads.

## Development environment setup:

This is a Kotlin Multiplatform project. This repository contains the Android app, web app, and
backend server source code in its modules. Android Studio is the development environment for the 
entire project. 

*  __Step 1: Download and install Android Studio__: If you don't already have the latest version, download 
from [https://developer.android.com/studio](https://developer.android.com/studio).

* __Step 2: Make sure that java is on your system path__: If you already have OpenJDK17+, you can use that.
If not, you can use the version that is bundled with Android Studio. 

Linux:
Option 1: Edit ~/.profile and add the following lines to the bottom (where Android Studio is installed in ~/android-studio):

```
export JAVA_HOME=~/android-studio/jbr
export PATH=$JAVA_HOME:PATH
```

Option 2: Install OpenJDK17+
```
sudo apt-get install openjdk-18-jdk
```

Windows: Search for "Environment Variables" - then "Edit the system environment variables". Create a
new environment variable. Set the variable name to JAVA_HOME and set the value to the directory 
where you have Android Studio\jbr e.g. C:\user\myusername\AndroidStudio\jbr where Android Studio is
installed in C:\user\myusername\AndroidStudio\ .

Now find the PATH variable. Append ```;%JAVA_HOME%\bin``` to the value and save it. 

Further details: see the [Java website](https://www.java.com/en/download/help/path.html).

* __Step 3: Import the project in Android Studio__: Select File, New, Project from Version Control. Enter
https://github.com/UstadMobile/UstadMobile.git and wait for the project to import.

* __Step 4: Build/run the server__: Run the server locally:

Linux/MacOS:
```
$ ./runserver.sh
```

Windows:
```
$ runserver.bat
```

This will start the server on port 8087. The admin password will be randomly generated - you can find
it in app-ktor-server/data/singleton/admin.txt .

This will run the REST API which is required by the Android and web apps. It will not include the 
web client app itself. To use the web client app in the browser, you must build/run it (as below).

* __Step 5: Build/run the Android and/or web client version__ : see [app-android](app-android/) for the
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

* [app-android](app-android/): Contains the Android app
* [app-react](app-react/): Contains the web app (written in Kotlin, using React via kotlin-wrappers)
* [app-ktor-server](app-ktor-server/): Contains the HTTP rest server (implemented using KTOR)
* [core](core/) : Contains view models, ui state, core business logic.
* [sharedse](sharedse/): Contains some shared implementations for operating systems with a disk (JVM/Android)
* [lib-database](lib-database/): contains the database, DAOs, and entity classes.
* [lib-util](lib-util/): Small utility functions
* [test-end-to-end](test-end-to-end/) End-to-end tests that run the app and server.
* [testserver-controller](testserver-controller/) An HTTP server that can control starting and 
  stopping the main server, and manage adb screen recording. Used by end-to-end testing.

To build / run versions for any given platform please see the README in the module for that platform.

