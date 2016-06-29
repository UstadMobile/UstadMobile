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

