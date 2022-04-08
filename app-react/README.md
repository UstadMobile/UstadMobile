# app-react

This modules contains react web application, based on kotlin multi-platform idea as it uses kotlin-wrappers to build UI based on MUI library

## Prerequisites
Install the [Latest Node](https://nodejs.org/en/download/) on your machine since the kotlin multiplatform eco-system depends on it.

## Development

Build, configure and then run the http server. Development is done using the KTOR HTTP server in
the app-ktor-server module to proxy requests as needed to the webpack development server (thus
avoiding cross origin request issues).

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
./runserver.sh
```

Start the webpack development server:

```
./gradlew app-react:browserDevelopmentRun --continuous
```

Open a web browser on the address/port of the ktor http server e.g. http://localhost:8087/


* Generating webpack bundle
This is a deployable bundle, it is standalone bundle which include everything needed for the app to run.
```
./gradlew app-react:browserDevelopmentWebpack
```

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

### Bundling with app-ktor-server

For simplicity, the web app is packaged on server as static files, so in order to get a deployable
 bundle of server and web app just run

```
./gradlew app-ktor-server:shadowJar
./runserver.sh
```
This will run ``` app-react:generateProductionBundle ``` which prepare and packages the web app into a server's jar
file.  After you build and start the server, you can simply open http://localhost:8087/ in the
web browser. See the [INSTALL](../INSTALL.md) for production installation instructions.

