# Cypress End-to-end tests

These are end-to-end tests for the web version of the app built using [Cypress](https://cypress.io).

Requirements:

* Install Node and NPM as per [NPM official docs](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm). 
  This is tested using NPM9 and 10 and Node 18.
* Install Cypress dependencies as [Cypress docs](https://docs.cypress.io/guides/continuous-integration/introduction#Dependencies)
* Build the app-ktor-server and testserver-controller e.g.
  ```./gradlew app-ktor-server:build testserver-controller:build  -Pktorbundleproductionjs=true```

This is currently running/tested on Ubuntu Linux. It should work on Windows, but this is not yet 
tested.

Running:
```
./run-cypress-test.sh
```
