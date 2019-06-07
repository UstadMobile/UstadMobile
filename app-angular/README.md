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

* **Generate Jar from Kotlin Multi-platform source**
```
./gradlew core:jsJar
```

* **Create Node package**
```
./gradlew app-angular:jar2npm
```
* **Simplified**
```
 ./gradlew app-angular:clean core:clean core:jsJar app-angular:jar2npm
```

After that your IDE should see that:
```
import {com} from 'core';
```
In case you want to use two different import with com as top package, use alias i.e
```
import {com as core} from 'core';
import {com as db} from 'lib-database';
```

* **RTL Support**

MaterializeCss doesn't support RTL out of the box, so in case you want to add new component to the app then follow the following procedure:

star:

- In the new component constructor call 
```
this.umService.isLTRDirectionality();
```
This will return true if the current directionality is LTR

- Make use of Materialize quick floats, right/left to position the view base on the active directionality i.e.
```
element1_class =  this.umService.isLTRDirectionality ? "right":"left";
element2_class =  this.umService.isLTRDirectionality ? "left":"right";
```
- Use created varibale to add a class to the HTML containers (div), conatiner's will interchange position based on the current system directionality.
```
<div class="row">
    <div class="col s12 l4 {{element1_class}}"></div>
    <div class="col s12 l8 {{element2_class}}"></div>
</div>
```
* **Running an app locally**
```
ng serve
```
This will create an app, to run it use http://localhost:4200/

## Deployment
To create production app, you need to generate JS bundles from angular source. To achieve that run the following command on your terminal.

```
npm rum um-build
```

