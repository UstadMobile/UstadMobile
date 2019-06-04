# Ustad Mobile core module

This module contains the main presenters, views, and utility classes. It is a Kotlin Multiplatform
module.

### Tests

Tests currently use the JVM variant to run. 

```
./gradlew core:jvmTest
```

#### Running tests in the IDE

Android Studio / IntelliJ behavior needs some manual corrections in order for tests to run.

Before using Android Studio to run tests, you need to build JVM tests

```
./gradlew core:jvmTestClasses
```

Issues and workarounds implemented: 

* System properties: System.getProperty values for things like java.home are null. The 
dumpEnvProperties task in core will dump the system properties using Gradle, so that it can be 
loaded and manually put in place using System.setProperty

* Resource loading: the IDE runs the tests from projectDir/classes .

