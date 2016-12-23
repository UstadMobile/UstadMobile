Ustad Mobile is a standards based open source mobile learning platform
for J2ME enabled feature phones and Android.

Content authoring can be done using any tool that supports EPUB.  We
recommend the open source eXeLearning authoring tool (see exelearning.net).

Ustad Mobile is licensed under the GPLv3 license: please see the LICENSE
file for details.

The platform consisits of a main cross platform 'core' and an
implementation of the UI and platform specific function in ports.

Most of the code lives in Gradle projects:
core : Contains core models and logic
ports/sharedse: Contains implementation for "Full Fat" Non-Micro implementations: e.g. Android, J2SE, iOS via J2ObjC
ports/android: Contains Android implementation
ports/j2me: This is an ant project: Contains J2ME Implementation

To build and run tests: 

1. Copy core/testsettings.gradle.example to core/testsettings.gradle

2. Make gradle copy the default app config: 
    cd core
    gradle initAppConfig

3. See the README file in ports/<platform name>

 






