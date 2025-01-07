# Cypress End-to-end tests

These are end-to-end tests for the web version of the app built using [Cypress](https://cypress.io).

Requirements:

* Install Node and NPM as per [NPM official docs](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm) 
  e.g. using node version manager. This is tested using NPM10 and Node 18.
* Install Cypress dependencies as [Cypress docs](https://docs.cypress.io/guides/continuous-integration/introduction#Dependencies)
* Build the app-ktor-server and testserver-controller (inc) e.g.
  
  Build the entire project with ```./gradlew build  -Pktorbundleproductionjs=true``` OR 
  ```./gradlew app-ktor-server:build testserver-controller:build  -Pktorbundleproductionjs=true```
  to build only the server and testserver-controller. 

 Important: Building MUST use ```-Pktorbundleproductionjs=true``` to include the production webapp
 in the server. See the building for production note in app-ktor-server/README.md.

* Run ```npm install``` to install NPM dependencies.

Running:

```
npm run test
```

Continuous integration (CI):

In a CI environment it is likely necessary to set the port to avoid potential conflict with other
job running.
```
export TESTCONTROLLER_URL=http://localhost:port/
npm run test-ci
```

How it works (in progress):

* package.json uses start-server-and-test command as [recommended by Cypress docs](https://docs.cypress.io/app/continuous-integration/overview#Solutions) to
  start the testserver-controller.
* testserver-controller is called by http request from the test spec and starts a blank new instance 
  of the actual server on request on a new port for each test (see testserver-controller docs).
