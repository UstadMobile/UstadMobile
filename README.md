Ustad Mobile is a standards based open source mobile learning platform
for J2ME enabled feature phones, Android and soon iOS.

Content authoring can be done using any tool that supports EPUB.  We
recommend the open source eXeLearning authoring tool (see exelearning.net).

Ustad Mobile is licensed under the GPLv3 license: please see the LICENSE file for details.

The platform consisits of a main cross platform 'core' and an
implementation of the UI and platform specific ports in app-platformname .

Most of the code lives in Gradle projects:
core : Contains core models and logic
sharedse: Contains implementation for "Full Fat" Non-Micro implementations: e.g. Android, J2SE, iOS via J2ObjC
app-android: Contains Android implementation
app-j2me: This is an ant project: Contains J2ME Implementation

To build and run tests: 

1. Copy keystore.properties.example to keystore.properties - use 
   this for signing credentials.  You can copy it and leave the values
   as they are for development/testing before signing.

2. Copy core/testsettings.gradle.example to core/testsettings.gradle

3. Make gradle copy the default app config: 
    cd core
    gradle initAppConfig

4. See the README file in app-<platform name>

 






