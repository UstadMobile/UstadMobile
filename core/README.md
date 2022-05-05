# Ustad Mobile core module

This module contains the main presenters, views, and utility classes. It is a Kotlin Multiplatform
module.

### Tests

Tests currently use the JVM variant to run. 

```
./gradlew core:jvmTest
```

### Coverage

```
./gradlew core:jvmTest core:jvmJacocoTestReport
```

#### Running tests in the IDE

Using the normal Android Studio / IntelliJ test runner does not work. Instead, select the gradle
test task from the Gradle menu on the right in Android studio (e.g. core/jvmTest). Use the
--tests argument to specify running a specific test in the options for the gradle task.

Issues and workarounds implemented: 

* System properties: System.getProperty values for things like java.home are null. The 
dumpEnvProperties task in core will dump the system properties using Gradle, so that it can be 
loaded and manually put in place using System.setProperty

* jsTest : This might throw an IllegalStateException when running. Run core:clean, core:prepareLocale,
   and try again.

