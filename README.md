# Ustad Mobile

Ustad Mobile is the offline app for better education. It features:
* Learning content delivery
* Attendance management
* Student record management

It's open source __and__ powered by open standards:

* [EPUB content](http://idpf.org/epub): Anything you can do with HTML5 can be in EPUB (video, interactive quizzes, etc).
* [Open Publication Distribution System (OPDS)](https://opds-spec.org): An Atom RSS feed describing and index of content. Well explained at [discover-opds.org](http://discover-opds.org).
* [Experience API](http://www.tincanapi.com): The open widely adopted standard to record learning experiences.

Ustad Mobile is licensed under the AGPLv3 license: please see the LICENSE file for details.

The platform consisits of a main cross platform 'core' and an implementation of the UI and platform specific ports in app-platformname .

Code lives mostly in gradle projects as follows:
* __core__ : Contains core models and logic.
* __core-tests__ : Contains testing code shared between implementations. Some tests of core functionality cannot run without an implementation: So the tests are placed in this separate module and then included as test dependencies for individual implementations (e.g. Android, J2ME, etc).
* __sharedse__: Contains implementation for "full fat" non-micro implementations: e.g. Android, J2SE, iOS via J2ObjC.
* __app-android__: Contains Android implementation, forms the basis of the app in app-android-launcher but can also be used as a library in other apps.
* __app-android-launcher__: Standalone app launcher that uses app-android.
* __app-j2me__: This is an ant project: Contains J2ME feature phone implementation.
* __app-ios__: The iOS port done using j2objc.

To build debug / release versions for any given platform please see the README in the directory for that platform.

## Configuration management

There are two parts to the configuration:

* Build configuration : buildconfig properties that are merged and turned into static constants,
  and can also be used to control the gradle build process itself.

* App config : a .properties file that is contained in the assets. This can be overriden when the
  app is used as a library. By default com/ustadmobile/core/appconfig.properties will be used. This
  can be overriden by setting the com.ustadmobile.core.appconfig manifest property (e.g. meta-data
  in AndroidManifest.xml or using properties in the jad file on J2ME).

The codebase is designed to make it easy to generate builds with custom content sets, branding, etc. These configuration sets are gitignored and saved to their own separate repository. 

The build configuration system uses .properties files in each module: 
* **buildconfig.default.properties**: Contains default options
* **buildconfig.local.properties** : Contains any custom options and is excluded from git. Any option set in buildconfig.local.properties will override what's set in the default properties.

Most core options (e.g. app name, base content catalog, etc) are in the core module (e.g. in [core/buildconfig.default.properties](core/buildconfig.default.properties)). Options specific to a given platform (e.g. the android application id) 
  are in the module for that platform (e.g. app-android/buildconfig.local.properties).

  
To commit the build config to it's own git repository (push):
 ```
 ./gradlew -PconfigRepoUri=user@server.com:/path/to/repo pushLocalConfig
```
  
To apply a new build config from a git repository (clone):
```
./gradlew -PconfigRepoUri=user@server.com:/path/to/repo cloneLocalConfig
```
  
 To pull changes from git for a build config that's currently applied (pull):
 ```
 ./gradlew pullLocalConfig
 ```

