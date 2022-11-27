# app-react

This module contains the web client application built using Kotlin/JS using React and MUI
via [kotlin-wrappers](https://github.com/JetBrains/kotlin-wrappers).

## Prerequisites
Install [Node 18x](https://nodejs.org/en/download/) on your machine since Kotlin/JS depends on it
to build.

## Development

Build, configure and then run the http server [app-ktor-server](app-ktor-server/README.md).
Development is done using the KTOR HTTP server in
the app-ktor-server module to proxy requests as needed to the webpack development server (thus
avoiding cross-origin request (CORS) issues). The webpack development server runs on port 8080 by
default.

Copy app-ktor-server/src/resources/application.conf to app-ktor-server/, then edit it and uncomment
the jsDevServer line

```
ustad {
...
jsDevServer = "http://localhost:8080/"
...
}
```

Run the http server:

On Linux:
```
$ ./runserver.sh
```
On Windows:
```
$ runserver.bat
```

Start the webpack development server (in another terminal):

```
$ gradlew app-react:browserDevelopmentRun --continuous
```

Open a web browser on the address/port of the ktor http server e.g. http://localhost:8087/


* Generating webpack bundle
This is a deployable bundle, it is standalone bundle which include everything needed for the app to run.
```
$ gradlew app-react:browserDevelopmentWebpack
```

### Production bundling with app-ktor-server

The production Javascript bundle will only be include with app-ktor-server if the ktorbundleproductionjs
argument is set as follows:

```
$ gradlew app-ktor-server:shadowJar -Pktorbundleproductionjs=true
```
