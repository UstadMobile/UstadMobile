J2ME Version

Setup build:
Copy the properties file: you can point to the Sun WTK installation:
```
$ cp ustadmobilemicro-build.default.properties ustadmobilemicro-build.properties
```
Run the getlibs script to download required jars:
```
$ ant -f antenna-build.xml -lib /path/to/antenna-dir getlibs
```
Run updatecore to copy the UstadMobile core library into place:
```
$ ./updatecore
```

To Build for debugging/emulator usage:

```
export WTK_HOME=/path/to/WTK
ant -f antenna-build.xml -lib /path/to/antenna-dir
```

To Build and Sign:

```
ant -f antenna-build.xml sign -lib /path/to/antenna-dir
```

To run/debug using MicroEmu:

* Create a plain Java project with the src and src-microemueun directories
* Run UstadMobileMicroEmuRun.java with microemulator.jar and microemu's JSR-75 and JSR-135 jars in the classpath

To setup Netbeans for Development

This uses two projects: one for the core library and one for the j2me port.

Setup Core Project:
* Click File - New Project - Java Project with existing Sources
* Give it a name (e.g. UstadMobileJava), click next.
* Add the source directory (core/src/) to source package folders
* Add the test directory (core/test) to the test package folders, Click Finish
* Open the project properties, Libraries, Click Add Jar/Folder, and select these jars from core/lib/ :
** kxml2-2.3.0.jar
** json-20141113.jar
** qrcode.jar

Setup Micro Edition Project

* Ensure that $ ./updatecore has been run as mentioned above
* Click File - New Project - Java ME - Mobile Project with Existing MIDP Sources
* Select ports/j2me/src as the Sources location.
* Select the Emulator of your choice and CLDC-1.1 / MIDP-2.0 Profile
* Right click on the project properties, select Libraries and Resources, add these jars from core/lib:
** j2meunit.jar
** LWUIT-MIDP.jar
** json-me.jar
** kxml2-min-2.3.0.jar



