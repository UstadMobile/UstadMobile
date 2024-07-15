
# app-ktor-server

This modules contains server application, responsible for handling all HTTP requests 
in both production and development environment. It is built as KTOR server.

# Development running:

2. Build and run using the runserver.sh script:

```
# Linux
$ ./runserver.sh --siteUrl http://your.ip.address:8087/

#Windows
$ runserver.bat --siteUrl http://your.ip.address:8087/
```

Where your.ip.address is your IP address or a domain/host URL that points to your system. 

You may set the siteUrl in the configuration file instead of specifying this on the command line.
Copy the default config file:
```
# Linux
cd app-ktor-server
cp src/main/resources/application.conf ./ustad-server.conf

# Windows
cd app-ktor-server
copy src\main\resources\application.conf .\ustad-server.conf
```
Then modify ustad-server.conf to set the siteUrl or other options.

Run ./runserver.sh --help to see options

You can also run the server by using the Java command directly:
```
cd app-ktor-server
java -jar build/libs/ustad-server-all.jar -config=my-application.conf
```

The port can be set using the command line as per KTOR server standard options, see
[ktor.io reference](https://ktor.io/docs/configurations.html#command-line).

The application can be debugged using the same as any other standalone JAR using JWDP. In Android
Studio or IntelliJ, Go to  run, debug, configurations and then add a "remote" configuration.

See [INSTALL.md](../INSTALL.md) for recommendations on production configuration.

# Production build

By default the Javascript (app-react) version will not be built and bundled in the static resources
directory (because this will slow down development including building jvm tests etc). When building
the server for production, use -Pktor bundleproductionjs to include the app-react web app as a
static resource.

Linux:
```
$ ./gradlew app-ktor-server:distZip -Pktorbundleproductionjs=true
```

Windows:
```
$ gradlew app-ktor-server:distZip -Pktorbundleproductionjs=true
```

This will build the server distribution zip ( in ```build/distributions/ustad-server.zip``` ) which can
then be used as per the [INSTALL.md documentation](../INSTALL.md).
