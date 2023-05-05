# app-react

This module contains the web client application built using Kotlin/JS using React and MUI
via [kotlin-wrappers](https://github.com/JetBrains/kotlin-wrappers). The UI follows the patterns 
found in [kotlin-mui-showcase](https://github.com/karakum-team/kotlin-mui-showcase).

Wrappers for other libraries (see package com.ustadmobile.wrappers ) created as per: 

* [JS in Kotlin/JS](https://dev.to/mpetuska/js-in-kotlinjs-c4g)
* [Kotlin for the JS Platform - Javascript Modules (official)](https://kotlinlang.org/docs/js-modules.html)
* [Kotlin for the JS Platform - Use dependencies from npm (official)](https://kotlinlang.org/docs/using-packages-from-npm.html)

The api url endpoint is normally determined automatically by checking the browser location href. You 
can override it by adding ```apiUrl=https://domain.myendpoint.com/path/``` to the url (value must by
url encoded as normal).

## MUI React/JS UI Development

Run the app-react module via Gradle:

```
./gradlew app-react:browserDevelopmentRun --continuous 
```
You can stop it by using Ctrl+C.

This will build the project and open a new browser window. The project will automatically recompile
when any source files are changed. You can refresh the project once it has recompiled to see the
changes. The browser might auto refresh.

To add a new screen:

1. Create a new Kotlin File in app-react source in the package com.ustadmobile.view . The file should
be named ScreeNameScreen e.g. PersonDetailScreen, PersonEditScreen, PersonListScreen etc.
2. Add a functional component and props. You can follow the example in PersonDetailScreen. The props  
should contain the UiState class and all event handlers.
3. Add the screen to useUstadScreens ( in com.ustadmobile.hooks ). This will add the screen to the
navigation bar on the left. You can now view the screen in the browser.

### Known Issues:

1. Do not import mui.icons.material.* - this will make autocomplete/intellisense unusably slow in the IDE.

## Development

Build, configure and then run the http server [app-ktor-server](app-ktor-server/README.md).
Development is done using the KTOR HTTP server in
the app-ktor-server module to proxy requests as needed to the webpack development server (thus
avoiding cross-origin request (CORS) issues). The webpack development server runs on port 8080 by
default.

Copy app-ktor-server/src/resources/application.conf to app-ktor-server/, then edit it and uncomment
the jsDevServer line

```
ustad {
...
jsDevServer = "http://localhost:8080/"
...
}
```

Run the http server:

On Linux:
```
$ ./runserver.sh
```
On Windows:
```
$ runserver.bat
```

Start the webpack development server (in another terminal):

```
$ gradlew app-react:browserDevelopmentRun --continuous
```

Open a web browser on the address/port of the ktor http server e.g. http://localhost:8087/


* Generating webpack bundle
This is a deployable bundle, it is standalone bundle which include everything needed for the app to run.
```
$ gradlew app-react:browserDevelopmentWebpack
```

### Production bundling with app-ktor-server

The production Javascript bundle will only be include with app-ktor-server if the ktorbundleproductionjs
argument is set as follows:

```
$ gradlew app-ktor-server:shadowJar -Pktorbundleproductionjs=true
```
