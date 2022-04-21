# app-react

This modules contains react web application, based on kotlin multi-platform idea as it uses
kotlin-wrappers to build UI based on MUI library

## Prerequisites
Install [Node 14x](https://nodejs.org/en/download/) on your machine since Kotlin/JS depends on it
to build.

## Development

Build, configure and then run the http server. Development is done using the KTOR HTTP server in
the app-ktor-server module to proxy requests as needed to the webpack development server (thus
avoiding cross origin request issues). The webpack development server runs on port 8080 by default.
This avoids cross-origin request (CORS) issues.

The server can be built using the normal Gradle build. If you want to build only the server on its
own, you can run:
```
./gradlew -Pskipreactproductionbundle=true app-ktor-server:shadowJar
```
Note: by default the server build will include the app-react production bundle. This is not needed
when running in development mode. It can be skipped to reduce the build time.

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

```
$ ./runserver.sh
```

Start the webpack development server (in another terminal):

```
$ ./gradlew app-react:browserDevelopmentRun --continuous
```

Open a web browser on the address/port of the ktor http server e.g. http://localhost:8087/


* Generating webpack bundle
This is a deployable bundle, it is standalone bundle which include everything needed for the app to run.
```
$ ./gradlew app-react:browserDevelopmentWebpack
```

### C

## Production
For production purpose, below are the equivalent commands
* Running the app locally in production mode

```
./gradlew app-react:browserProductionRun
```
* Building and running continuously

```
./gradlew app-react:browserProductionRun --continuous
```
* Generating webpack bundle

```
./gradlew app-react:browserProductionWebpack
```

### Production bundling with app-ktor-server

For simplicity, the web app is packaged on server as static files, so in order to get a deployable
 bundle of server and web app just run

```
./gradlew app-ktor-server:shadowJar
./runserver.sh
```

This will run ``` app-react:generateProductionBundle ``` which prepare and packages the web app into a server's jar
file.  After you build and start the server, you can simply open http://localhost:8087/ in the
web browser. See the [INSTALL](../INSTALL.md) for production installation instructions.

