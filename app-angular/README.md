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
Install all NPM packages used in the app

```
npm install
```

## Deployment
To create deployable app, you need to generate JS bundles from angular src. To achieve that run the following command on your terminal.

```
npm build
```

