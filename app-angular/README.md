# UstadMobile AppAngular App

Ustad Mobile enables learners to access and share content offline. It uses peer-to-peer networking 
(including WiFi Direct) to enable offline sharing between devices. It's open source __and__ 
powered by open standards:  

* [EPUB content](http://idpf.org/epub): Anything you can do with HTML5 can be in EPUB (video, interactive quizzes, etc).
* [Experience API](http://www.tincanapi.com): The open widely adopted standard to record learning experiences.

Ustad Mobile is licensed under the AGPLv3 license: please see the LICENSE file for details.

## Prerequisites
After acquiring a copy of this app, the first thing to do is to install [Node](https://nodejs.org/en/download/) v8 or above (Everything depends on it). After Node installation, install angular CLI. Finally, install all dependencies used in the project since node_modules directory is git ignored. 

* Installing Angular CLI
```
npm install -g @angular/cli
```
* Installing all dependencies
```
npm install
```

## Development
We have used [Jar2Npm Plugin](https://github.com/svok/kotlin-jar2npm-plugin) to create Node package from kotlin multiplatform generated Jar file. To create node package follow the following procedures.

* Generate Jar from Kotlin Multi-platform source
```
./gradlew core:jsJar
```

* Create Node package
```
./gradlew :app-angular:jar2npm
```

After that your IDE should see that:
```
import {com} from 'core';
```
* Running an app locally
```
ng serve
```
This will create an app, to run it use http://localhost:4200/

## Deployment
To create production app, you need to generate JS bundles from angular source. To achieve that run the following command on your terminal.

```
npm build --prod
```

