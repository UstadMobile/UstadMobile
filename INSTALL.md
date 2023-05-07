# Installing Ustad Mobile on your own server

The Ustad Mobile server runs as a standalone Java JAR file (powered by KTOR and Netty) It. can be
connected to an SQLite or PostgreSQL database. It is recommended to use Apache or Nginx as a proxy.
The server should run fine on any platform that supports JVM (Linux, Windows, Mac, etc), but it has
been most extensively tested on Ubuntu Linux.

## Quickstart

### 1. Download server jar and Android APK from GitHub release
See releases on [https://www.github.com/UstadMobile/UstadMobile](https://www.github.com/UstadMobile/UstadMobile)

### 2. Install requirements:

On Ubuntu:
```
apt-get install ffmpeg openjdk-18-jdk
```
Note: if you have other Java versions, make sure you run the server jar using JDK17+. You can use ``sudo update-alternatives --config java``
to set the default java version to run.

On Windows:
* Download [FFMPEG Windows Build](https://www.gyan.dev/ffmpeg/builds/)
* Copy the binaries to the working directory (e.g. the same directory where ustad-server-all.jar is)
* Alternatively, place the ffmpeg binaries anywhere you wish, and add them to your PATH environment variable

### 3. Run the server jar to start the server

```
$ java -jar ustad-server-all.jar
```
This starts the server on the default port (8087).

### 4. Install the APK and connect to server

Drag and drop the APK onto the emulator, or use the command:

```
adb install app-android-launcher-release.apk
```

When the app prompts you for a link, you should enter the link using the IP address of the server
e.g. http://192.168.0.10:8087/ where 192.168.0.10 is the IP address of the PC running the server.
Using localhost on an Android device or emulator will **NOT** work. If you are using an Android
emulator, you can use 10.0.2.2 which always points to the Android emulator host device.

A random admin password will be generated automatically. It will be placed in
**data/singleton/admin.txt**.

## Server configuration (optional)

You may create a configuration file to set the port, database, and other options. Copy/paste the file below and edit as required. When you run the server, specify the path to the config file e.g.

```
java -jar ustad-server-all.jar -config=my-configuration.conf
```

Configuration file:

```
ktor {
    deployment {
        port = 8087

        # Uncomment this to enable SSL. If this is enabled, a certificate must be provided (see
        # security section below)
        # sslPort = 8889

    }

    application {
        modules = [ com.ustadmobile.lib.rest.UmRestApplicationKt.umRestApplication ]
    }

    ustad {
        # dbmode can be singleton or virtualhost .  If the dbmode is virtualhost, then multiple
        # instances can run using the same server.
        dbmode = singleton
        datadir = data

        ##Enables development mode which will enables CORS and allow to clear all tables
        devmode = "false"

        # The app download link for Android users. Users will be redirected here if they select
        # to download the app
        androidDownloadHref = "https://play.google.com/store/apps/details?id=com.toughra.ustadmobile"

        # Running the web version (app-react) using Cross Origin requests etc. can cause issues.
        #
        # Setting jsDevServer will forward all requests not expected to be handled by KTOR to
        # the given jsDevServer (e.g. acting as a reverse proxy). This is normally the server that
        # is started using
        # ./gradlew app-react:browserDevelopmentRun --continuous
        #
        # This makes it possible to use browserDevelopmentRun for fast compilation (e.g. no need
        # to build a production bundle) and avoid cross origin request permission issues.
        #
        # jsDevServer = "http://localhost:8080/"

        paths {
            # These are external commands that are required. Normally they will be automatically
            # detected in the path, and there is no need to specify them manually

            # If they are not in the path or default location, then specify them below

            # FFMPEG is used for media compression
            # ffmpeg = /usr/bin/ffmpeg
            # ffprobe = /usr/bin/ffprobe
        }
    }

    database {
        #Change to "org.postgresql.Driver" to use Postgres. Postgres is recommended for production
        #use
        driver = "org.sqlite.JDBC"

        # If you are using the virtualhost dbmode, then you will need to add the (hostname)
        # variable to the database url e.g.
        #   url=jdbc:postgresql:///ustad_(hostname)
        # Any non alphanumeric characters in hostname (e.g. ., -, etc) will be replaced with _
        #
        # If you are simply running a single instance (e.g. no virtual hosting), just enter the JDBC
        # database url here.
        #
        # For Sqlite:
        #  jdbc:sqlite:path/to/file.sqlite?parameters
        # recommended parameters: journal_mode=WAL&synchronous=OFF&busy_timeout=30000&recursive_triggers=true
        # journal_mode and synchronous parameters improve performance ( https://sqlite.org/wal.html )
        # busy_timeout is the time that SQLite will wait for a commit to finish
        # recursive_triggers are required to correctly execute progress trackers for ContentJobItem
        # For Postgres
        #  jdbc:postgresql:///dbName
        url = "jdbc:sqlite:data/singleton/UmAppDatabase.sqlite?journal_mode=WAL&synchronous=OFF&busy_timeout=30000&recursive_triggers=true"

        # Enter the Postgres database username and password here if using Postgres. If using SQLite,
        # these can be left blank
        user = ""
        password = ""
    }

    # The database for Quartz is stored separately. Quartz is a library used to trigger timed tasks.
    # There is always one (and only one) quartz database, even when there are multiple virtual
    # servers.
    quartz {
        # Change to org.postgresql.Driver to use Postgres. SQLite is fine for Quartz for production
        # and testing use.
        driver = "org.sqlite.JDBC"


        url = "jdbc:sqlite:data/quartz.sqlite?journal_mode=WAL&synchronous=OFF&busy_timeout=30000"

        user = ""
        password = ""
    }

    security {
        # It is strongly recommended to configure SSL here so the app can use HTTP2. See INSTALL.md
        # for instructions. This applies even when the app is used behind a reverse proxy (because
        # HTTP2 defacto requires SSL).
        # ssl {
        #     keyStore = /etc/letsencrypt/live/mydomain.com/keystore.jks
        #     keyAlias = myalias
        #     keyStorePassword = password
        #     privateKeyPassword = password
        # }
    }
}

#
# Sending email is required to allow parents to approve a registration for their child.
#
# mail {
#    user = ""
#    auth = ""
#    from = ""
#
#    # These properties are as per the Jakarta mail property options:
#    #
#    # https://jakarta.ee/specifications/mail/1.6/apidocs/index.html?com/sun/mail/smtp/package-summary.html
#    #
#    smtp {
#        auth = "true"
#        host = ""
#        port = 587
#        starttls {
#            enable = "true"
#        }
#    }
# }
```

## Production recommendations

* Use an HTTP server such as Apache or Nginx with a reverse proxy. Apache or Nginx
  should be used to provide https support e.g. as per [Apache Reverse Proxy Guide](https://httpd.apache.org/docs/2.4/howto/reverse_proxy.html).

* Setup a Postgres database and use this instead of the default (embedded) SQLite.

* Run the server using a script on startup or use the screen command.

