# Ustad Mobile

![Ustad Mobile app screenshots](img/readme/readme-screenshots.png)

Ustad Mobile enables users to teach, learn, and share: online or offline. It is a learning
management system (LMS) built from the ground up to work with or without connectivity. Features 
include:

* Online and offline usage of eLearning content with support for Experience API (xAPI), H5P, ePub, 
  PDF, and videos.
* Assignments where students can submit work to be marked by their teacher or other students (peer
  marking). 
* Discussion boards
* Attendance and enrolment tracking
* Course structuring for course authors to structure content, assignments, discussion boards, text, 
  and (optionally) assign points for completion.
* Offline sync that works via any Internet connection or via nearby devices (e.g. from teacher device
  to student device etc). With a class of 30 students this reduces bandwidth consumption 97%+ whilst
  supporting data sync to the Internet when a connection is available.

Want to collaborate on development? Join us on discord: [https://discord.gg/WHe35Sbsg4](https://discord.gg/WHe35Sbsg4).  

## Translations

Ustad Mobile is currently available in English, Dari, Pashto, and Tajik. Translation is done using 
on our [Weblate project](https://hosted.weblate.org/projects/ustad-mobile/). You do not need to be a
programmer! Please register on Weblate and then [create an issue](https://github.com/UstadMobile/UstadMobile/issues/new)
with the subject "Localization - language name" and let us know what language you would like to
translate into. We can then give you permission to start translating!

Translations are stored as Android strings XML resource files in [core/src/commonMain/resources/MR](core/src/commonMain/resources/MR).
The project uses [Moko-Resources](https://github.com/icerockdev/moko-resources) to make strings 
available on all platforms (including JVM and Javascript).

If adding a new language it should be added to the constant in in 
[SupportedLanguagesConfig.kt](core/src/commonMain/kotlin/com/ustadmobile/core/impl/config/SupportedLanguagesConfig.kt)
 and app-android/build.gradle resConfigs should be updated.

## Contributing

Contributions are welcome, there are many ways to contribute as a developer, translator, or user. 
See the [CONTRIBUTING.md](CONTRIBUTING.md) for details. If you're unsure
about anything, please join our [discord server](https://discord.gg/WHe35Sbsg4) or create an issue
here on GitHub and label it as a question.

## Documentation for users

The documentation here on Github is intended for those who are contributing to the project (inc 
translation, software development, bug reporting, testing, etc) and technical users (e.g. developers, 
server admins). If you want documentation for end users, please see the manual on ReadTheDocs at 
[https://ustadmobile.readthedocs.org/](https://ustadmobile.readthedocs.org/)

## Installing on a server (self-hosting)

See [INSTALL.md](INSTALL.md) for instructions for installation on your own server using binary 
downloads.

## Development environment setup:

These instructions are intended for developers who wish to build/run from source code. 

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
https://github.com/UstadMobile/UstadMobile.git and wait for the project to import. Switch to the
  dev-mvvm-primary branch (Menu: Git - Branches - search for dev-mvvm-primary - checkout ).

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


### Code structure

This multi-module Gradle project built using Kotlin Multiplatform. It builds for:

* Android (app-android)
* KTOR Server (JVM) (app-ktor-server)

Additional information on coding structure is available as follows:

* [ARCHITECTURE.md](ARCHITECTURE.md) - architecture overview of the tech stack.
* [CODING-STYLE.md](CODING-STYLE.md) - coding style including how MVVM is applied using Kotlin 
  Multiplatform.
* [DBSCHEMA.md](DBSCHEMA.md) - Database schema of the database tables used by the app.

Code is contained (mostly) in the following modules:

* [app-android](app-android/): Contains the Android app
* [app-desktop](app-desktop/): Work in progress: contains Desktop app based on [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/).
* [app-react](app-react/): Contains the web app (written in Kotlin, using React via kotlin-wrappers)
* [app-ktor-server](app-ktor-server/): Contains the HTTP rest server (implemented using KTOR)
* [core](core/) : Contains view models, ui state, core business logic.
* [sharedse](sharedse/): Contains some shared implementations for operating systems with a disk (JVM/Android)
* [lib-database](lib-database/): contains the database: DAOs (e.g. SQL queries), and entity classes.
* [lib-ui-compose](lib-ui-compose/): contains Compose multiplatform UI code used by app-android and app-desktop
* [lib-util](lib-util/): Small utility functions
* [test-end-to-end](test-end-to-end/) End-to-end tests that run the app and server.
* [testserver-controller](testserver-controller/) An HTTP server that can control starting and 
  stopping the main server, and manage adb screen recording. Used by end-to-end testing.

To build / run versions for any given platform please see the README in the module for that platform.

## Legal and license

Copyright 2015-2023 UstadMobile FZ-LLC.
Documentation: [CC-BY](https://creativecommons.org/licenses/by/4.0/) license.
Code and all other works: [AGPLv3](LICENSE) license.
