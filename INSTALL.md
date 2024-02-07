# Installing Ustad Mobile on your own server

The Ustad Mobile server runs as a standalone Java JAR file (powered by KTOR and Netty) It. can be
connected to an SQLite or PostgreSQL database. It is recommended to use Apache or Nginx as a proxy.
The server should run fine on any platform that supports JVM (Linux, Windows, Mac, etc), but it has
been most extensively tested on Ubuntu Linux.

## Quickstart

### 1. Download ustad-server.zip and app-android-release.apk from GitHub release
See releases on [https://www.github.com/UstadMobile/UstadMobile/releases](https://www.github.com/UstadMobile/UstadMobile/releases)

### 2. Install server requirements:

The server requires JDK17+. The __java__ command should be in the PATH or the JAVA_HOME variable should
be set (this is done automatically by default when you install Java from an installer package e.g. 
using apt-get on Ubuntu or MSI/EXE for Windows).

On Ubuntu:
```
apt-get install openjdk-18-jdk ffmpeg
```
Note: if you have other Java versions, make sure you run the server jar using JDK17+. You can use ``sudo update-alternatives --config java``
to set the default java version to run.

On Windows:
* Download and install Java (JDK17+) if not already installed from the Java site [https://www.oracle.com/java/technologies/downloads/#jdk17-windows](https://www.oracle.com/java/technologies/downloads/#jdk17-windows)
* FFMPEG is required. If you don't already have it in your path, the server can download it for you 
  when you run it for the first time.

### 3. Unzip ustad-server.zip and start server

Unzip ustad-server.zip. Open the ustad-server.conf file and set the siteUrl property to the url that 
will be used to access the site e.g. https://ustad.yourdomain.com/ (e.g. using a reverse proxy setup
with Apache or Nginx in a production setup) or http://your.ip.address:8087/ (for testing/evaluation).

If you use localhost or 127.0.0.1, you will not be able to connect from Android (because localhost
on an emulator or device refers to the Android emulator/device, not the PC running the server).

e.g.
```
ktor {
    ..
    ustad {
        # Uncomment the siteUrl line found here to set the siteUrl
        siteUrl = "http://192.168.1.2:8087/"
        ..
    }
}        
```

If your site will allow self-registration (which can be enabled by the admin user through settings
after logging in), you must configure the email section of the config file. The Children's Online 
Privacy Protection Act requires obtaining of parental consent, which is done by requesting a parental
email address. Uncomment the mail section in ustad-server.conf and add an email account that can be
used to send email.

After setting the siteUrl in the configuration file (and email config if required), start the server:

Linux/MacOS:
```
cd /path/to/unzipped/ustad-server/
./bin/ustad-server
```
Where /path/to/unzipped/ is where you unzipped ustad-server.zip

Windows
```
cd C:\User\me\path\to\unzipped\ustad-server
.\bin\ustad-server
```
Where C:\User\me\path\to\unzipped\ is where you unzipped ustad-server.zip

This starts the server on the default port (8087). You can now open a browser, and use the url
you specified as the siteUrl.

A random admin password will be generated automatically. It will be placed in
**data/singleton/admin.txt**.

### 4. Install the APK and connect to server

Drag and drop the APK onto the emulator, or use the command:

```
adb install app-android-launcher-release.apk
```

When the app prompts you for a link, you should enter the link using the IP address of the server
e.g. http://192.168.0.10:8087/ where 192.168.0.10 is the IP address of the PC running the server.
Using localhost on an Android device or emulator will **NOT** work. If you are using an Android
emulator, you can use 10.0.2.2 which always points to the Android emulator host device.


## Customize server configuration (optional)

You may create a update the configuration in ustad-server.conf to set the database, directory where
server data is stored, and other options. 

## Production recommendations

* Use an HTTP server such as Apache or Nginx with a reverse proxy. Apache or Nginx
  should be used to provide https support e.g. as per [Apache Reverse Proxy Guide](https://httpd.apache.org/docs/2.4/howto/reverse_proxy.html).
  The [Forwarded](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Forwarded) header must
  include the protocol (e.g. http or https) or the [X-Forwarded-Proto](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-Proto)
  header must be set. 

Apache example:
```
# AllowEncodedSlashes must be enabled, otherwise blob paths will not work
AllowEncodedSlashes On

#Nocanon is required to ensure that encoding 
ProxyPass / http://localhost:8087/ nocanon
ProxyPassReverse / http://localhost:8087/
SSLProxyEngine On
ProxyPreserveHost On
RequestHeader set X-Forwarded-Proto https
```

* Setup a Postgres database and use this instead of the default (embedded) SQLite.

* Run the server using a script on startup or use the screen command.

