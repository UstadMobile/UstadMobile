# app-react

This modules contains react web application, based on kotlin multi-platform idea as it uses kotlin-wrappers to build UI based on MUI library

## Prerequisites
Install the [Latest Node](https://nodejs.org/en/download/) on your machine since the kotlin multiplatform eco-system depends on it.

## Development
You can now build this module by simply running the commands listed below. It will download all required
dependencies for it to start-up. You can run on either development or production mode while doing
development to make sure it behaves the same on both dev and production environment respectively.

```
./gradlew app-react:browserDevelopmentRun
```

This will create an app and it will automatically open http://localhost:8080/ in the system default
browser. This won't really work because the react client depends on accessing the http API, which
won't be available on this server.

There are two workarounds:

1)  Enable CORS and different server with browserDevelopmentRun, first set devmode = true in
the app-ktor-server application.conf (so cross origin requests will be allowed). This will work for
everything except content (eg epubs, videos, etc that use iframes).

```
ktor {
    ...
    ustad {
        ...
        devmode = true
    }
}
```

Now start the server:

```
./gradlew -Pskipreactproductionbundle=true app-ktor-server:shadowJar
./runserver.sh
```
Note: skipreactproductionbundle will skip building the production react app that would otherwise get
bundled into the server jar. This will make the development build much faster.

Then open http://localhost:8080/#/LoginView?apiUrl=http://localhost:8087/ where 8087 is the port
being used by the server (the default)

2)  Use the [proxy](./contrib/kotlinjs-apache-debug.conf) configuration. The proxy will send api
requests to app-ktor-server, and the rest will be handled through the development server.

Start the server:
```
./gradlew -Pskipreactproductionbundle=true app-ktor-server:shadowJar
./runserver.sh
```

Now open the port as per the proxy config, e.g. http://localhost:8091/

* Building and running continuously
This will rebuild and re-run the app as you change some files
```
./gradlew app-react:browserDevelopmentRun --continuous
```

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
### Deploying
For simplicity, the web app is packaged on server as static files, so in order to get a deployable
 bundle of server and web app just run

```
./gradlew app-ktor-server:shadowJar
./runserver.sh
```
This will run ``` app-react:generateProductionBundle ``` which prepare and packages the web app into a server's jar
file.  After you build and start the server, you can simply open http://localhost:8087/ in the
web browser. See the [INSTALL](../INSTALL.md) for production installation instructions.

