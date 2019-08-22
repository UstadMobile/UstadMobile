
# app-angular  
This modules contains angular web application, based on kotlin multi-platform idea it uses packages delivered from kotlin based modules packaged as NPM packages.  
  
## Prerequisites  
After acquiring a copy of this app, the first thing to do is to install [Node v8](https://nodejs.org/en/download/)  or above (Everything depends on it). After Node installation, install angular CLI.   
Finally,change working directory to app-angular and install all dependencies used in the project since node_modules directory is git ignored.   
  
* Installing Angular CLI  
	```  
	npm install -g @angular/cli  
	```  
* Resolving EACCES Permission errors  
If using Linux or MacOS, you need to change your npm global directory as per [Resolving EACCES permissions errors when installing packages globally](https://docs.npmjs.com/resolving-eacces-permissions-errors-when-installing-packages-globally)   
otherwise you will have file permission errors on build.  
  
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
