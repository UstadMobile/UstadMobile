J2ME Version

Setup build:
```
$ cp ustadmobilemicro-build.default.properties ustadmobilemicro-build.properties
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

