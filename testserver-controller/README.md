## Testserver-controller

This provides a KTOR http server that can control starting and stopping the actual server (e.g. the 
one in [app-ktor-server module](../app-ktor-server/)). This is used by end-to-end tests to start a 
new (blank) instance of the server before each test.

Usage:

Start the test server controller:

```
./gradlew testserver-controller:run --args='-P:mode=cypress|maestro [-P:url=http://localhost:8075/] [-P:portRange=1025-65534] [-P:learningSpaceUrlTemplate=http://{HOSTIP}:{PORT}/]'
```
If no URL is specified testserver-controller will automatically use http://localhost:8075/ by 
default.

```-P:portRange```: Specifies the port range that will be used to run the actual server. This is 
useful in a CI environment where a specific port range is allowed by the firewall.
```-P:learningSpaceUrlTemplate```: Specifies the URL to use for the learning space, based on the
allocated port/host ip address. ```{HOSTIP}``` will be replaced with the first non-local IPv4 address
and ```{PORT}``` will be replaced with the allocated port. This can also be used to run tests over
https e.g. as required for testing links and passkeys on Android e.g. use 
https://{PORT}.wildcardhttpsdomain.org/ when a reverse proxy is setup to handle the HTTPS.

Testserver-controller has two modes:

* __Cypress__
  * One instance of the actual server is run at a time.
  * A reverse proxy is provided so that Cypress test specs can use a single baseUrl. testcontroller-server
    will start a new actual server on any available random port.
  * Tests are expected to run on localhost.
* __Maestro__
  * Multiple instances of the actual server can run at a time to support [Maestro sharding](https://blog.mobile.dev/whats-new-in-maestro-1-37-0-581431428562)
    such that multiple tests can run concurrently on multiple emulators.
  * The emulator or device connects directly to the actual server started by testcontroller-server 
    using the IP address of the PC (laptop/server etc) running the tests and the randomly allocated
    port.

```
http://localhost:8075/testcontroller/start
```



Clearing Postgres data:

It is recommended to use Postgres for end-to-end testing to ensure that the test matches the 
production environment. This will require the database to be cleared before each test. This
will drop all triggers, functions, and tables on the database set (so be careful).

1. Copy the configuration template:
```
cp testserver-controller/src/main/resources/application.conf testserver-controller/testserver-controller.conf
```

2. Uncomment and set the clearPgUrl, clearPgUser, clearPgPass properties

The file testserver-controller.conf is covered by .gitignore to avoid Postgres usernames and 
passwords being committed to git.
