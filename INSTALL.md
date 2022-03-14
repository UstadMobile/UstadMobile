# Installing Ustad Mobile on your own server

The Ustad Mobile server runs as a standalone Java JAR file (powered by KTOR and Netty) It. can be
connected to an SQLite or PostgreSQL database. It is recommended to use Apache or Nginx as a proxy.
The server should run fine on any JVM platform, but has been tested most extensively on Linux
(Ubuntu 20.04).

## Create a user, directory, and copy files

Create a user (optional) and a directory where the server and its data will be stored.
```
$ adduser ustad
$ sudo -u ustad -s
$ mkdir /home/ustad/server

# Copy the server JAR
$ cp /my/Download/ustad-server-all.jar /home/ustad/ustad-server-all.jar

# Install java (on Ubuntu)
$ apt-get install openjdk-11-jre ffmpeg
```

# Install FFMPEG (required)

FFMPEG is required for media conversion purposes. If ffmpeg and ffprobe are in the path, they
will be found automatically.

On Ubuntu:
```
apt-get install ffmpeg
```

On Windows:
* Download [FFMPEG Windows Build](https://www.gyan.dev/ffmpeg/builds/)
* Copy the binaries to the working directory (e.g. the same directory where ustad-server-all.jar is)
* Alternatively, place the ffmpeg binaries anywhere you wish, then set the path in application.conf
  (see section below)



## Install HTTPS Certificate (recommended)

It is strongly recommended to use https and HTTP2 in production. Install a certificate for your
server using [EFF Certbot](https://certbot.eff.org/).

Request a certificate as follows if a certificate has not already been generated. Turn off any
 server running on port 80 when requesting the certificate.

```
export DOMAIN=mydomain.com
export EMAIL=me@mydomain.com
export ALIAS=myalias
certbot certonly -n -d $DOMAIN --email "$EMAIL" --agree-tos --standalone --preferred-challenges http
```

Convert the letsencrypt (or other) certificate to a jks (as per [KTOR https setup instructions](https://ktor.io/docs/ssl.html#ktor))
```
openssl pkcs12 -export -out /etc/letsencrypt/live/$DOMAIN/keystore.p12 -inkey /etc/letsencrypt/live/$DOMAIN/privkey.pem -in /etc/letsencrypt/live/$DOMAIN/fullchain.pem -name $ALIAS
# Provide a password

keytool -importkeystore -alias $ALIAS -destkeystore /etc/letsencrypt/live/$DOMAIN/keystore.jks -srcstoretype PKCS12 -srckeystore /etc/letsencrypt/live/$DOMAIN/keystore.p12
```

## Configure the server:

Unzip the example config file and edit it as required:
```
unzip ustad-server-all.jar application.conf
```

Edit the server config file as required to enable HTTPS/SSL and set the database url:
```
ktor {
    deployment {
        port = 8087

        # Uncomment this to enable SSL. If this is enabled, a certificate must be provided (see
        # security section below)
        sslPort = 8889

    }

    application {
        modules = [ com.ustadmobile.lib.rest.UmRestApplicationKt.umRestApplication ]
    }

    ustad {
        # dbmode can be singleton or virtualhost .  If the dbmode is virtualhost, then multiple
        # instances can run using the same server.
        dbmode = singleton
        datadir = data

        # The app download link for Android users. Users will be redirected here if they select
        # to download the app
        androidDownloadHref = "https://play.google.com/store/apps/details?id=com.toughra.ustadmobile"

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
        #Change to org.postgresql.Driver to use Postgres. Postgres is recommended for production
        #use
        driver = "org.sqlite.JDBC"

        # If you are using the virtualhost dbmode, then you will need to add the (hostname)
        # variable to the database url e.g.
        #   url=jdbc:postgres:///ustad_(hostname)
        # Any non alphanumeric characters in hostname (e.g. ., -, etc) will be replaced with _
        #
        # If you are simply running a single instance (e.g. no virtual hosting), just enter the JDBC
        # database url here.
        url = "jdbc:sqlite:data/singleton/UmAppDatabase.sqlite?journal_mode=WAL&synchronous=OFF&busy_timeout=30000"

        # Enter the Postgres database username and password here if using Postgres. If using SQLite,
        # these can be left blank
        user = ""
        password = ""
    }

    security {
        # It is strongly recommended to configure SSL here so the app can use HTTP2. See INSTALL.md
        # for instructions. This applies even when the app is used behind a reverse proxy (because
        # HTTP2 defacto requires SSL).
        ssl {
             keyStore = /etc/letsencrypt/live/mydomain.com/keystore.jks
             keyAlias = myalias
             keyStorePassword = password
             privateKeyPassword = password
        }
    }
}
```

## Run the server

Run the server using the Java command:

```
java -jar ustad-server-all.jar -config=application.conf
```

## Configure Apache HTTP Proxy (optional)

Enable the required modules:
```
sudo a2enmod proxy_http2 http2
```


Add the following to your Apache virtual host:
```
Protocols h2 http/1.1
ProxyPass /ustad/ h2://your.server.name:8889/
ProxyPassReverse /ustad/ https://your.server.name:8889/
SSLProxyEngine On

TimeOut 600
ProxyTimeout 600
RequestReadTimeout body=10,MinRate=1000
```

HTTP2 requires Apache mpm-event and will not work with mpm-prefork and the normal vanilla php.
The FastCGI php version must be used instead if PHP is required.

```
a2dismod mpm_prefork
a2enmod mpm_event

#If PHP is also required on the same Apache server:
apt-get install php7.4-fpm
a2enmod proxy_fcgi setenvif
a2enconf php7.4-fpm
a2dismod php7.4
```

## Connect using the Ustad app

Open the Ustad app, and enter the address of your site. The admin password will be saved to
data/singleton/admin.txt

You can now login to the app with the username admin and the password contained in admin.txt
