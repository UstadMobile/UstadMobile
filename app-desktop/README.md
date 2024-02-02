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
export CONVEYOR_SITE_URL=https://server.com/to/conveyor/
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

