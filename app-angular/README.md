# UstadMobile AppAngular App

Ustad Mobile enables learners to access and share content offline. It uses peer-to-peer networking 
(including WiFi Direct) to enable offline sharing between devices. It's open source __and__ 
powered by open standards:  

* [EPUB content](http://idpf.org/epub): Anything you can do with HTML5 can be in EPUB (video, interactive quizzes, etc).
* [Experience API](http://www.tincanapi.com): The open widely adopted standard to record learning experiences.

Ustad Mobile is licensed under the AGPLv3 license: please see the LICENSE file for details.

## Prerequisites
After acquiring a copy of this app, the first thing to do is to install [Node](https://nodejs.org/en/download/) v8 or above (Everything depends on it). After Node installation, install angular CLI. Finally,change working directory to app-angular and install all dependencies used in the project since node_modules directory is git ignored. 

* Installing Angular CLI
```
npm install -g @angular/cli
```

## Development
While you are still in app-angular , you can now build this module. It will create modules dependencies from core source ready to be installed.

* Building and installing
```
./gradlew app-angular:build
```

After that your IDE should see all the module dependencies and should be used like this:
```
import entity from 'UstadMobile-lib-database-entities';

//it can be used by specifying full path to the class or object
i.e
const contentEntry = new entity.com.ustadmobile.lib.db.entities.ContentEntry();
```
* Running an app locally (More info on how to customize this like specifying ports and e.t.c, check angular offical website)
```
ng serve
```
This will create an app, to run it use http://localhost:4200/

## Deployment
To create production app, you need to generate JS bundles from angular source. To achieve that run the following command on your terminal.

```
npm build --prod
```

