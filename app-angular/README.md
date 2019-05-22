# UstadMobile AppAngular App

Ustad Mobile enables learners to access and share content offline. It uses peer-to-peer networking 
(including WiFi Direct) to enable offline sharing between devices. It's open source __and__ 
powered by open standards:  

* [EPUB content](http://idpf.org/epub): Anything you can do with HTML5 can be in EPUB (video, interactive quizzes, etc).
* [Experience API](http://www.tincanapi.com): The open widely adopted standard to record learning experiences.

Ustad Mobile is licensed under the AGPLv3 license: please see the LICENSE file for details.

### Prerequisites
After acquiring a copy of this app, the first thing to do is to install node. Make sure you install v8 or above (Everything depends on it). After that make sure to install all NPM packages used in the project since node_modules directoey is git ignored. 

### Installing
Install all Node packages used in the app

```
npm install
```

## Development
We have used [Jar2Npm Plugin](https://github.com/svok/kotlin-jar2npm-plugin) to create Node package from kotlin multiplatform generated Jar file. To create node package follow the following procedures.

* Compile Kotlin JS to get source files
```
./gradlew core:compileKotlinJs
```

* Generate Jar from compiled source
```
./gradlew core:jsJar
```

* Create Node package
```
./gradlew :app-angular:jar2npm
```

After that your IDE should see that:
```
import {ContentEntryDetailView} from 'core';
```


## Deployment
To create deployable app, you need to generate JS bundles from angular source. To achieve that run the following command on your terminal.

```
npm build --prod
```

