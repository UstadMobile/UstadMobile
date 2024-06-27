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

## Getting started from source

* Follow steps in the [main project README](../README.md#development-environment-setup) to setup development
  environment and start the server.

* Run the web app using Gradle:

Linux/MacOS
```
$ ./gradlew app-react:jsRun
```
Windows:

```
$ gradlew app-react:jsRun
```
Both the webpack development server (started by ```app-react:jsRun```) _and_ the main http
server (started by the runserver command as per main project README step 4) must run simultaneously
to run the Javascript version from source in development mode.

See [Kotlin/JS docs](https://kotlinlang.org/docs/running-kotlin-js.html#run-the-browser-target) for
further info on Gradle commands if required.

The browser will open the webpack development server on port 8080. Close that tab, and then open 
http://localhost:8087/ in a new tab. You should now see the web client.

Note: you can add --continuous to use [continuous compilation](https://kotlinlang.org/docs/dev-server-continuous-compilation.html)
to enable automatic continuous compilation.

### Webpack server note:

The server module will handle all normal REST API requests and
proxy any other requests to the webpack development server. This avoids browser
[Cross Origin Requests (CORS)](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS) restrictions
that would happen if the rest API is on a different server or port to the server providing the
web client (e.g. the webpack development server).

### Known Issues:
1. Click to copy will not work when used in a non-secure context (e.g. not localhost or using https).
2. Do not import mui.icons.material.* - this will make autocomplete/intellisense unusably slow in the 
   IDE. Use a named import to ensure the wildcard is not put there by the IDE later e.g. 
   import mui.icons.material.Folder as FolderIcon

### Building for production

The production bundle is included as static assets when building the server for production. 

The production Javascript bundle will only be include with app-ktor-server if the ktorbundleproductionjs
argument is set as follows:

Linux/MacOS:
```
$ ./gradlew app-ktor-server:distZip -Pktorbundleproductionjs=true
```

Windows:
```
$ gradlew app-ktor-server:distZip -Pktorbundleproductionjs=true
```

### Updating license information

1. Install npm generate-license-file globally e.g.
```
npm install -g generate-license-file
```
2. Run script:
```
./generate-license-file.sh
```
