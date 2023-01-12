## Testserver-controller

This provides a KTOR http server that can control starting and stopping the actual server. This is 
used by end-to-end tests to start a new (blank) instance of the server before each test.

WARNING: this will automatically delete all app-ktor-server data (the data directory and database)
every time the actual server is started.

This is not tested or supported on Windows. It might (or might not) work

Usage:

Start the test server controller:
```
./start.sh 
```

To start/restart the actual server (e.g. to run an end-to-end test), request the start url:

```
http://localhost:8075/start
```

If any actual server is running that was started by the control server, it will be assumed that this
was from a previous test and it will be stopped. The database and data directory will be automatically
cleared

Stop the control server:

```
./stop.sh
```
