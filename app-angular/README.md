  
# app-angular 
This modules contains angular web application, based on kotlin multi-platform idea it uses packages delivered from kotlin based modules packaged as NPM packages.    
    
## Prerequisites

After acquiring a copy of this app, the first thing to do is to install [Node v8](https://nodejs.org/en/download/)  or above (Everything depends on it). After Node installation, install angular CLI.    
Finally,change working directory to app-angular and install all dependencies used in the project since node_modules directory is git ignored.     
    
* Installing Angular CLI    
  
``` npm install -g @angular/cli ```
* Resolving EACCES Permission errors  
  
If using Linux or MacOS, you need to change your npm global directory as per [Resolving EACCES permissions errors when installing packages globally](https://docs.npmjs.com/resolving-eacces-permissions-errors-when-installing-packages-globally)    
otherwise you will have file permission errors on build.    
  
* Install protractor globally  
  
```npm install protractor -g```

 This will install protractor and webdriver-manager, then run   
 
 ```webdriver-manager update ```
 
For more information checkout [here](http://www.protractortest.org/#/tutorial)  
  
## Development

While you are still in app-angular , you can now build this module. It will create modules dependencies from core source ready to be installed.    
    
* Building and installing

```./gradlew app-angular:build```

After that your IDE should see all the module dependencies and should be used like this: 
 
```
import entity from 'UstadMobile-lib-database-entities';    
 //it can be used by specifying full path to the class or object i.e const contentEntry = new entity.com.ustadmobile.lib.db.entities.ContentEntry();
``` 
 
* Run app locally  
  
**Run with default locale**

```ng serve  or  npm run start```

**Running app locally for a specific locale**

```
npm run start:<locale code>   
//i.e npm run start:en  
```

This will create an app, to run it use http://localhost:4200/   

**NOTE:**  

Make sure you build and run development server before executing above command, to run the server in development mode use:-  
  
```./gradlew app-ktor-server:appRunDevMode```

### Codding pattern  

#### Adding new component

To add new component, just run the following angular CLI commands:-  
  
```
ng generate component <path to views dir> <component name>  
i.e 
ng g c com/ustadmobile/view/new-component-name  
```

#### Adding new routes  
  
After adding the component to the src, then after you have to create   
it's route in **src/app/app-routing.module.ts**. All routes descends from **Home** route so to achieve that follow the following steps.  
  
* Go into **src/app/app.module.ts**

* Under **declarations** add your component name and make sure you add missing import

```
i.e import { NewComponentNameComponent } from "./com/ustadmobile/view/new-component-name/NewComponentNameComponent";  
```

* Go into **src/app/app-routing.module.ts**.  

* Under **Home** as root route, look for **children** array
  
* Add new route object item

```
 {path: "<path name>", component: <component name>}  
 i.e {path: "newPath", component: NewComponentNameComponent} //This will be resolved to Home/newPath
```

* If you want to add new root routes like **Home** or **NotFound**, just place route object below either of the two.  

For more information on router, routes and navigation checkout [here](https://angular.io/guide/router)  



#### RTL Support

MaterializeCss doesn't support RTL out of the box, so in case you want to add new component to the app then follow the following procedure:  

* In the new component constructor check if the UI is RTL or LTR, this will return **true** if the current directionality is LTR **false** otherwise

```this.umService.isLTRDirectionality();```

* Make use of Materialize quick floats, **right/left** to position the view base on the active directionality 
i.e.  

```
element1_class =  this.umService.isLTRDirectionality ? "right":"left";  
element2_class =  this.umService.isLTRDirectionality ? "left":"right";  
```

* Use created variable to add a class to the HTML containers (div), container's will interchange position based on the current system directionality.  

```
<div class="row">
	<div class="col s12 l4 {{element1_class}}">Left Column</div
	<div class="col s12 l8 {{element2_class}}">Right Column</div>
</div>  
```


## Testing 
You may opt to use angular or gradle tasks to run end to end tests  with Protractor,
for Karma it is more preferable to use gradle tasks.  
  
* Angular test framework  
  
With angular testing framework, don't forget to run development server  
  
i.e

```./gradlew app-ktor-server:appRunDevMode```

Then, you are good to start angular test  
  
```
ng e2e
//In-case you need to specify a port  
ng e2e --port <port number>
``` 
 
* Gradle tasks  
  
With this, you don't have to start development server since it will be started when needed and shut-down when done with testing.  
  
    
```
./gradlew app-angular:ngTest -Ptestmodule=e2e   
//specify port if needed, otherwise test will use default angulat port i.e 4200  
./gradlew app-angular:ngTest -Ptestmodule=e2e -Ptestport=<port number>  
```

To execute component tests with Karma use:- 
 
```
./gradlew app-angular:ngTest -Ptestmodule=<component name>   
i.e  
./gradlew app-angular:ngTest -Ptestmodule=home  
```


## Deployment 

To create production app, you need to generate JS bundles from angular source. To achieve that run the following command on your terminal.    
  
**Deploy for specific locale**  
  
```
npm run build:<locale code> //i.d npm run build:en  
or   
./gradlew app-angular:buildProd -Plocale=en  
```

**Deploy for all locales**  

```
npm run build-prod   
or  
./gradlew app-angular:buildProd  
  
//In case it will be deployed on a specific directory then add base href

./gradlew app-angular:buildProd -PbaseHref=<Directory name>  
```
