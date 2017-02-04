#J2ME Version

The J2ME version should work on most CLDC-1.0 MIDP 2.0 models such as Nokia series 40 etc.
It uses the LWUIT user interface toolkit to implement the view layer.

## Prerequesites
* Install a compatible Wireless Toolkit (WTK). You can use the [Sun WTK](www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-javame-419430.html) on Linux or Windows.
* Install the Android SDK. This is needed by the core build.
* Install Apache ant

### Download libraries (one time)
Use the ant getlibs task to download required libraries (j2me JSON libs, testing libs, etc)
```
$ export WTK_HOME=/path/to/WTK ; export ANDROID_HOME=/path/to/android
$ ant -f antenna-build.xml getlibs
```

### Build for distribution
This is done using an ant build with Antenna (which was automatically downloaded by the getlibs task)
```
$ export WTK_HOME=/path/to/WTK ; export ANDROID_HOME=/path/to/android
$ ant -f antenna-build.xml
```
Jad and Jar files will be generated in dist-ANTENNA

### Build and sign
If an app is not signed by a certificate issued by a recognized certificate authority most devices either won't allow it to be granted file access permissions or will insist on prompting for permission every time a file is accessed
```
cp buildconfig.j2me.default.properties buildconfig.j2me.local.properties
#Edit buildconfig.j2me.local.properties to set the signing keystore, alias, password
ant -f antenna-build.xml sign
```

### Develop using Netbeans

* Install the Netbeans Java Me plugins (under Tools - Plugins - Search for J2ME)
* Click File - New Project - Java ME - Mobile Project with Existing MIDP Sources
* Select ports/j2me/src as the Sources location.
* Select the Emulator of your choice and CLDC-1.1 / MIDP-2.0 Profile
* Right click on the project properties, select Libraries and Resources, add these jars from core/lib:
 * j2meunit.jar
 * LWUIT-MIDP.jar
 * json-me.jar
 * kxml2-min-2.3.0.jar


### To run/debug using MicroEmu (unsupported):

* Create a plain Java project with the src and src-microemueun directories
* Run UstadMobileMicroEmuRun.java with microemulator.jar and microemu's JSR-75 and JSR-135 jars in the classpath



