# Ustad Mobile

Ustad Mobile enables learners to access and share content offline. It uses peer-to-peer networking 
(including WiFi Direct) to enable offline sharing between devices. It's open source __and__ 
powered by open standards:  

* [EPUB content](http://idpf.org/epub): Anything you can do with HTML5 can be in EPUB (video, interactive quizzes, etc).
* [Experience API](http://www.tincanapi.com): The open widely adopted standard to record learning experiences.

Ustad Mobile is licensed under the AGPLv3 license: please see the LICENSE file for details.

The platform consisits of a main cross platform 'core' and an implementation of the UI and platform specific ports in app-platformname .

Code is contained (mostly) in the following modules:
* [core](core/) : Contains Presenters, Views Interfaces, and core business logic.
* [core-tests](core-tests/) : Contains testing code shared between implementations. Some tests of core functionality cannot run without an implementation: So the tests are placed in this separate module and then included as test dependencies for individual implementations.
* [sharedse](sharedse/): Contains implementation for operating systems with a disk
* [app-android](app-android/): Contains Android implementation, forms the basis of the app in
  app-android-launcher but can also be used as a library in other apps.
* [app-android-launcher/](app-android-launcher/): Standalone app launcher that uses app-android.
* [app-android-testserver](app-android-testserver/): Provides a test server for p2p functionality integration testing.
* [lib-core-fs](lib-core-fs/): Contains code used on platforms that have a file system. To be
merged into sharedse.
* [lib-database](lib-database/): core Database and Data Access Object (DAO) classes. See this
module's README for further details on the relationship between different database modules.
* [lib-database-android](lib-database-android/) Android database
implementation (a Room Persistence Framework database generated from lib-database).
* [lib-database-annotation](lib-database-annotation/): annotation classes used for entities
 and DAOs.
* [lib-database-annotation-processor-core](lib-database-annotation-processor-core/): database annotation processor
  used to generate DAOs and database classes for different platforms.
* [lib-database-entities](lib-database-entities/) contains database POJO entity classes.
* [lib-database-entity-processor](lib-database-entity-processor/) executable that adds Room Persistence Framework
annotations to POJOs from lib-entities and outputs them to lib-database-android.
* [lib-util](lib-util/): Small utility functions


To build debug / release versions for any given platform please see the README in the directory for that platform.

## Configuration management

There are two parts to the configuration:

* Build configuration : buildconfig.properties contains properties used
during the build and testing process.

* App config : a .properties file that is contained in the assets. This can be overriden when the
  app is used as a library. By default com/ustadmobile/core/appconfig.properties will be used. This
  can be overriden by setting the com.ustadmobile.core.appconfig manifest property (e.g. meta-data
  in AndroidManifest.xml).

The build configuration system uses .properties files in each module: 
* **buildconfig.default.properties**: Contains default options
* **buildconfig.local.properties** : Contains any custom options and is excluded from git. Any option set in buildconfig.local.properties will override what's set in the default properties.


