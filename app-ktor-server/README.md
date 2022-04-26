
# app-ktor-server

This modules contains server application, responsible for handling all HTTP requests 
in both production and development environment. It is built as KTOR server.

# Development running:

1. Build the fat jar:
```
$ gradlew app-ktor-server:shadowJar
```

This builds the server jar which can be found in app-ktor-server/build/libs/ustad-server-all.jar .

2. Copy the default configuration file and adjust it for your database and/or https certificate (optional)

```
cd app-ktor-server
cp src/jvmMain/resources/application.conf ./
```

3. Run using the runserver.sh script:

```
# Linux
$ ./runserver.sh

#Windows
$ runserver.bat
```

Or use the Java command
```
cd app-ktor-server
java -jar build/libs/ustad-server-all.jar -config=application.conf
```

The port can be set using the command line as per KTOR server standard options, see
[ktor.io reference](https://ktor.io/docs/configurations.html#command-line).

The application can be debugged using the same as any other standalone JAR using JWDP. In Android
Studio or IntelliJ, Go to  run, debug, configurations and then add a "remote" configuration.

See [INSTALL.md](../INSTALL.md) for recommendations on production configuration.

#Production build

By default the Javascript (app-react) version will not be built and bundled in the static resources
directory (because this will slow down development including building jvm tests etc). When building
the server for production, use -Pktorbundleproductionjs to include the app-react web app as a
static resource:

```
./gradlew app-ktor-server:shadowJar -Pktorbundleproductionjs=true
```