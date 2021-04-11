
# app-ktor-server

This modules contains server application, responsible for handling all HTTP requests 
in both production and development environment. It is built as KTOR server.

# Running

1. Build the Fat JAR
```
./gradlew app-ktor-server:shadowJar
```

2. Copy the default configuration file and adjust it for your database and/or https certificate

```
cd app-ktor-server
cp src/jvmMain/resources/application.conf ./
```

2. Run using the Java command

```
cd app-ktor-server
java -jar build/libs/ustad-server-all.jar -config=application.conf
```

The port can be set using the command line as per KTOR server standard options, see
[ktor.io reference](https://ktor.io/docs/configurations.html#command-line).

The application can be debugged using the same as any other standalone JAR using JWDP. In Android
Studio or IntelliJ, Go to  run, debug, configurations and then add a "remote" configuration.

See [INSTALL.md](../INSTALL.md) for recommendations on production configuration.
