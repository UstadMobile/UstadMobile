# app-desktop

This is a desktop version of the application created using [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)

## Getting started from source

* Follow steps in the [main project README](../README.md#development-environment-setup) to setup development
  environment and start the server.

* Run the web app using Gradle:

Linux/MacOS
```
$ ./gradlew app-desktop:run
```
Windows:

```
$ gradlew app-desktop:run
```

## Building for production

The main production build uses [Conveyor](https://conveyor.hydraulic.dev/) to build installers for
Linux, Windows, and Mac.

Install conveyor as per [Conveyor - Getting Started](https://conveyor.hydraulic.dev/13.0/).

Make installer for local use:

```
export CONVEYOR_SITE_URL=http://localhost/
export CONVEYOR_APP_DISPLAY_NAME="Ustad Mobile"
export CONVEYOR_APP_FS_NAME="ustad-mobile"
conveyor make site
```

Serve the download site locally:

```
cd output
npx serve
```


Deploy to server:

The output directory from conveyor should be copied to be accessible on the URL specified via the
CONVEYOR_SITE_URL environment variable.

## Troubleshooting

Developer mode in app:

Developer options can be enabled in the app by going to settings, then clicking the version number
seven times (e.g. as is done on Android). Developer settings will show the paths being used in the 
app for storing user data, logs, and the app itself. The logs include everything that is logged via
Napier, so can be very useful for diagnosing any edge case or hard to reproduce issues.

Running in IDE:

Clicking on the run button that appears in the source code next to the main function  **WILL NOT WORK**, 
it will not find the resource bundles required (probably due to the joys of Modular Java).

Use ./gradlew app-desktop:run to run it. To debug, run the Gradle app-desktop:run task in debug
mode in the IDE. There are two ways to do this:

* Run anything shortcut: Hit the ctrl key twice. This brings up the run anything shortcut. Enter
  ```gradlew app-desktop:run```. Hit enter to run the app. Then app-desktop will appear in the 
  configurations dropdown (next to run and debug) in the top right. It can then be run or debugged.
* Configurations menu:  
![runtime configurations screenshot](doc-img/ide-configurations.png)  
Click the configurations dropdown as above, click Edit Configurations, then click add. Select Gradle
from the list of task types. In the run field enter ```app-desktop:run```. You can now use this
configuration to run or debug the desktop version.

Commits in Android Studio:

Commits will miss changes to app-desktop/app-resources/common , even though they are not covered by
gitignore. These must be added via the command line.

Checking JAR size 

If, after changing dependencies, there are issues running the release version (e.g. shrunk with
proguard), it can be useful to check JAR size to assess if it is worth making more aggressive proguard
keep rules:
```
./gradlew app-desktop:proguardReleaseJars
du -sbh app-desktop/build/compose/tmp/main-release/proguard
```