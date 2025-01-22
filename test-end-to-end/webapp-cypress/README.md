# Cypress End-to-end testing for web

These are end-to-end tests for the web version of the app built using [Cypress](https://cypress.io).

How it works:

* package.json uses start-server-and-test command as [recommended by Cypress docs](https://docs.cypress.io/app/continuous-integration/overview#Solutions) to
  start the testserver-controller, wait for the testserver-controller to be ready, and then starts
  cypress run.
* The Cypress baseUrl is set to the testserver-controller. Each Cypress test spec makes an http
  request to the testserver-controller, which starts a blank new instance of the actual ustad server
  (e.g. the app-ktor-server module) on a random port for each test. The test calls
  the testserver-controller stop endpoint as part of the tearDown.

### Prerequisites:

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

### Running

Run the full suite of tests:
```
npm run test
```

Run a single test:

Option 1) Use npm on command line to run a specific spec

```
export CYPRESS_SPEC=cypress/e2e/testname.cy.js
npm run test-spec
```

Option 2) Run the testserver-controller manually, then use Cypress open

Use Gradle to run testserver controller (run from project root directory)
```
./gradlew testserver-controller:run --args='-P:mode=cypress'
```
Then use cypress open to run/debug test specs (run from webapp-cypress directory):
```
npm exec cypress open
```

### Continuous integration run

In a CI environment it is likely necessary to set the port to avoid potential conflict with other
job running.
```
export TESTCONTROLLER_URL=http://localhost:port/
npm run test-ci
```

__Outputs__: videos can be found in test-end-to-end/webapp-cypress/cypress/videos . Javascript 
console logs and cypress logs can be found in test-end-to-end/webapp-cypress/logs/cypress-logs.

