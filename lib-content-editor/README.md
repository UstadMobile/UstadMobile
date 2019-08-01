  
# Ustadmobile Content Editor (UmContentEditor)    
 TinyMCE based content editor with the ability to use custom html content templates.  Right now we support two question based template which are multiple choice and fill in the blanks.    
## Getting Started 
You can use this repo as stand-alone project out of Ustadmobile app (use on web apps),     
with Ustadmobile app this repo will be used as support lib since editor controls are implemented on native android.     
    
### Setup 
All plugins that will be used for testing requires Node v8.0.0 or higher. Node installation guide can be found on their official website.  
#### mocha-chrome
Plugin which will be used to run all our tests  
```
 //For testing  
 npm install -g mocha-chrome --save-dev 
```

#### Minifiers
Plugins which will be used for JS and CSS files minification
```
 //For JS files minification
 npm install uglify-es -g

 //For CSS files minification
 npm install --save-dev clean-css
``` 
#### Setting up custom minifiers path
Open buildconfig.local.properties and add the following variables
```
#path to cleancss executable (used for minifying css files)#
editor.cleancss = 

#path to uglifyjs executable (used for minifying js files)#
editor.uglifyjs = 
``` 



### Prerequisites 
* Make sure you install chrome before running any tests using mocha-chrome.  
* Make sure you run all the tests under local host or any active server environment.  
    
### Using the repo 
#### As stand-alone lib (on web apps) 
* Clone this repo to your local machine.     
* Create html file i.e index.html and import all necessary libraries.    
    
``` html 
<link rel="stylesheet" href="UmEditorCore.min.css">
<link rel="stylesheet" href="lib/material-icon/material-icons.css">
<script src="jquery3.3.1.min.js" type="text/javascript"></script>
<script src="lib/tinymce/js/tinymce.min.js" type="text/javascript"></script>
<script src="UmWidgetManager.min.js" type="text/javascript"></script>
<script src="UmEditorCore.min.js" type="text/javascript"></script>
```

* Call onCreate with locale and directionality values to make 
```javascript    
 window.onload = () => {
      $(".um-editor").css("min-height",$(window).height());
      UmEditorCore.onCreate("sw", "ltr", false);
  }
``` 
* To enable editing call enableEditingMode 

```javascript    
UmEditorCore.enableEditingMode();
```
    
 #### As android support lib (android)

 See how we used it to implement our editor on dev-content-editor branch, use ContentEditorActivity.java to understand the logical flow    
    
## Running the tests    
 You can quickly run tests on web browser, using mocha-chrome or using gradle.


**Note:**

This lib is fully tested using Google Chrome, in case of misbehaving on other web browsers kindly rise an issue.

**Run on web browser**

Run the following files located under tests directory on your browser  
  
```  
1. content-formatting-tests.html
  
2. content-language-locale-tests.html  
  
3. content-template-tests.html
  
```   
  **Run with mocha-chrome - Terminal**

Navigate to test directory and run the following command on your terminal


  
``` 
mocha-chrome content-formatting-tests.html --timeout 6000  
  
mocha-chrome content-template-tests.html --timeout 6000  
  
mocha-chrome content-language-locale-tests.html --timeout 6000
``` 
  
**Run with gradle**
  
```   
 ./gradlew :lib-content-editor:test  
```
## Playground
You can use index.html as your playground during development or just to check if things work, or to test how the lib works from native point of view.
This playground file will give you the core functionality of the lib like inserting question templates and multimedia content, inserting and removing links e.t.c.

## Adding custom template
 You may easily create your own html content template and add it to the template directory,   with its functionality implemented on both  UmEditorCore.js and  UmWidgetManager.js located on src directory.
