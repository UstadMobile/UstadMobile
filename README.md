# Ustad Mobile

Ustad Mobile is a standards based open source mobile learning platform for J2ME enabled feature phones, Android and soon iOS.

Content authoring can be done using any tool that supports EPUB.  We recommend the open source eXeLearning authoring tool (see exelearning.net).

Ustad Mobile is licensed under the GPLv3 license: please see the LICENSE file for details.

The platform consisits of a main cross platform 'core' and an implementation of the UI and platform specific ports in app-platformname .

Code lives mostly in gradle projects as follows:
* _core_ : Contains core models and logic
* _sharedse_: Contains implementation for "Full Fat" Non-Micro implementations: e.g. Android, J2SE, iOS via J2ObjC
* _app-android_: Contains Android implementation
* _app-j2me_: This is an ant project: Contains J2ME Implementation
* _app-ios_: The iOS port done using j2objc.

To build debug / release versions for any given platform please see the README in the directory for that platform.







