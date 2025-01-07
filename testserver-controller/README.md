## Testserver-controller

This provides a KTOR http server that can control starting and stopping the actual server. This is 
used by end-to-end tests to start a new (blank) instance of the server before each test.

WARNING: this will automatically delete all app-ktor-server data (the data directory and database)
every time the actual server is started.

This is not tested or supported on Windows. It might (or might not) work

Usage:

Start the test server controller:

```
./gradlew testserver-controller:run [--args='http://localhost:port/']
```
If no URL is specified testserver-controller will automatically use http://localhost:8075/ by 
default.

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
