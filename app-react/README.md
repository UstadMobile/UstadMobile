# app-react

This modules contains react web application, based on kotlin multi-platform idea as it uses kotlin-wrappers to build UI based on MUI library

## Prerequisites
Install the [Latest Node](https://nodejs.org/en/download/) on your machine since the kotlin multiplatform eco-system depends on it.

## Development
You can now build this module by simply run the commands listed below. It will download all required dependencies for it to start-up. You can run on either development or production mode while doing development to make sure it behaves the same on both dev and production environment respectively.

* Running an app locally, by default it runs on port 8080 but you can use [proxy](./contrib/kotlinjs-apache-debug.conf) since it has some iframe which will throw CORS when the contents are not on the same server.
	```
	./gradlew app-react:browserDevelopmentRun
	```
This will create an app, to run it use http://localhost:8080/

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
For simplicity, the web app is packaged on server as static files, so in order to get a deployable bundle of server and web app just run

```
./gradlew app-ktor-server:shadowJar
```
This will run ``` app-react:distro ``` which prepare and packages the web app into a server's jar file. Once a jar file is created, just follow the insturctions from  [README](../app-ktor-server/README.md) and project [INSTALL](../INSTALL.md) instruction respectively.
