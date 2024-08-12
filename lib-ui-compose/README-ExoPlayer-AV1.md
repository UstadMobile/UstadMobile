## ExoPlayer AV1 

Google doesn't make the LibAV1 decoder available as an artifact. The AAR can be built by following
the instructions on the [AV1 module readme](https://github.com/androidx/media/tree/release/libraries/decoder_av1).

This will generate an AAR. Directly using aar's and jars is no longer supported on Android builds. 
The module has no publish task. So one must manually create it in a local repository.

```
$ mkdir -p REPO_PATH/androidx/media3/lib-decoder-av1/1.3.0
$ cp CHECKOUT_PATH/libraries/decoder_av1/buildout/outputs/aar/lib-decoder-av1-release.aar \ 
     REPO_PATH/androidx/media3/lib-decoder-av1/1.3.0/lib-decoder-av1-1.3.0.aar
```
Where:
REPO_PATH is a maven repository root (local e.g. .m2/repository or http)
CHECKOUT_PATH is where the media3 repository has been cloned to
1.3.0 is the version (as per media3 library)

Now manually create a .pom file in ```REPO_PATH/androidx/media3/lib-decoder-av1/1.3.0/lib-decoder-av1-1.3.0.pom```

Contents:
```
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>androidx.media3</groupId>
  <artifactId>lib-decoder-av1</artifactId>
  <version>1.3.0</version>
  <packaging>aar</packaging>
</project>
```
