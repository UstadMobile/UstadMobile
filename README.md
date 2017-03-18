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
* __core__ : Contains core models and logic
* __sharedse__: Contains implementation for "full fat" non-micro implementations: e.g. Android, J2SE, iOS via J2ObjC
* __app-android__: Contains Android implementation.
* __app-j2me__: This is an ant project: Contains J2ME feature phone mplementation.
* __app-ios__: The iOS port done using j2objc.

To build debug / release versions for any given platform please see the README in the directory for that platform.

## Build configuration management

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

