# Installing Ustad Mobile on your own server

The Ustad Mobile server runs as a standalone Java JAR file (powered by KTOR and Netty) It. can be
connected to an SQLite or PostgreSQL database. It is recommended to use Apache or Nginx as a proxy.
The server should run fine on any platform that supports JVM (Linux, Windows, Mac, etc), but it has
been most extensively tested on Ubuntu Linux.

## Quickstart

### 1. Get ustad-server.zip distribution

Download from GitHub releases [https://www.github.com/UstadMobile/UstadMobile/releases](https://www.github.com/UstadMobile/UstadMobile/releases) or
build ustad-server.zip from source as per the __Production build__ procedure in 
[app-ktor-server/README.md](app-ktor-server/README.md).

### 2. Install server requirements:

The server requires JDK17+. The __java__ command should be in the PATH or the JAVA_HOME variable should
be set (this is done automatically by default when you install Java from an installer package e.g. 
using apt-get on Ubuntu or MSI/EXE for Windows).

On Ubuntu 23.10+:

Install required packages:
```
apt-get install openjdk-17-jdk mediainfo sox libsox-fmt-all vlc handbrake-cli
```

On Ubuntu (prior versions):

Install required packages:
```
apt-get install openjdk-17-jdk mediainfo sox libsox-fmt-all vlc flatpak
```

Previous versions (including 22.04 LTS) package HandBrake 1.5 (which is not supported due to lack of
AV1 support). You need to install the latest HandBrake CLI using flatpak as per [HandBrake website](https://handbrake.fr/downloads2.php):
```
flatpak install /path/where/downloaded/HandBrakeCLI-1.7.3-x86_64.flatpak
```

Note: if you have other Java versions, make sure you run the server jar using JDK17+. You can use 
``sudo update-alternatives --config java`` to set the default java version to run.

On Windows:
* Download and install Java (JDK17+) if not already installed from the Java site [https://www.oracle.com/java/technologies/downloads/#jdk17-windows](https://www.oracle.com/java/technologies/downloads/#jdk17-windows)
* Use Winget to download and install MediaInfo and HandBrakeCLI:
```
winget install -e --id MediaArea.MediaInfo
winget install -e --id HandBrake.HandBrake.CLI
```
* Download and install from the [Sox website](https://sourceforge.net/projects/sox/files/sox/14.4.2/) 
  (the Winget package does not work because it does not get added to the path).
* Download and install a __64bit version__ of VLC from the [VLC website](https://videolan.org)

### 3. Unzip ustad-server.zip and start server

Unzip ustad-server.zip. 

#### Single learning space system configuration

Open the ustad-server.conf file and set the siteUrl property to the url that 
will be used to access the site e.g. https://ustad.yourdomain.com/ (e.g. using a reverse proxy setup
with Apache or Nginx in a production setup) or http://your.ip.address:8087/ (for testing/evaluation).
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

#### Multi learning space system (virtual hosting) configuration

The Ustad server supports running multiple subdomains (e.g. schoolname1.example.org, schoolname2.example.org),
where each subdomain has a separate learning space (classes, users, content, etc) in a single JVM
(to significantly reduce memory/space overhead). Each learning space has its own database
(Postgres or SQLite). The server will then use a separate database for each learning space, and select
the database to use for a request based on the virtual host name.

This can be setup by setting dbmode to virtualhost and using (hostname) in the database url such that
each virtual host maps to a different database:

```
ktor {
  ..
  ustad {
    ..
    dbmode = virtualhost
    ..
    database {
       ..
       # For SQLite 
       url = "jdbc:sqlite:(datadir)/(hostname)/UmAppDatabase.sqlite?journal_mode=WAL&synchronous=OFF&busy_timeout=30000&recursive_triggers=true"
       ..
       
       # For Postgres - the username/password must be granted permission on all databases to be used
       url = "jdbc:postgresql:///ustad_(hostname)"
       ..
    }
  }
}
```
(hostname) will be automatically replaced with the hostname based on the incoming request - any
non-alphanumeric character will be replaced with _.

When using the virtual hosting mode, siteUrl is NOT set.

#### Email configuration

If your site will allow self-registration (which can be enabled by the admin user through settings
after logging in), you must configure the email section of the config file. The Children's Online 
Privacy Protection Act requires obtaining of parental consent, which is done by requesting a parental
email address. Uncomment the mail section in ustad-server.conf and add an email account that can be
used to send email.

#### Running the server

After setting the siteUrl (if not using virtual hosting) in the configuration file 
(and email config if required), start the server:

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

### Use a reverse proxy

Use an HTTP server such as Apache or Nginx with a reverse proxy. Apache or Nginx
should be used to provide https support e.g. as per [Apache Reverse Proxy Guide](https://httpd.apache.org/docs/2.4/howto/reverse_proxy.html).
The [Forwarded](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Forwarded) header must include the protocol (e.g. http or https) or 
the [X-Forwarded-Proto](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-Proto) 
header must be set. 

Apache example:
```
# AllowEncodedSlashes must be enabled, otherwise blob paths will not work
AllowEncodedSlashes On

#Nocanon is required to ensure that encoding in paths is not altered
ProxyPass / http://localhost:8087/ nocanon
ProxyPassReverse / http://localhost:8087/
SSLProxyEngine On
ProxyPreserveHost On
RequestHeader set X-Forwarded-Proto https
```

URL prefixes: if the system is not accessed via the top level URL, then the prefix must be specified
in ustad-server.conf using the urlPrefix property. The full URL must be passed e.g.
```
ProxyPass /ustad/ http://localhost:8087/ustad/ nocanon
ProxyPassReverse /ustad/ http://localhost:8087/ustad/
```

If using virtual hosting, then set ServerName and ServerAlias e.g.

```
ServerName example.org
ServerAlias *.example.org
```

Enable required Apache modules:
```
a2enmod proxy headers
```
Recommended:
```
a2enmod deflate
```
Apache mod deflate will compress the web version itself e.g. its own javascript, stylesheets, etc. 
Content assets will be compressed by the server itself.

* Setup a Postgres database and use this instead of the default (embedded) SQLite.

* Run the server using a script on startup or use the screen command.
