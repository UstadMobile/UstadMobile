const { defineConfig } = require("cypress");
module.exports = defineConfig({
    video: true,
    e2e: {
        chromeWebSecurity: false,
        experimentalSessionAndOrigin: true,
        reporter: 'mocha-junit-reporter', //refer here for report options: https://www.npmjs.com/package/mocha-junit-reporter#full-configuration-options
        reporterOptions: {
            mochaFile: 'results/my-test-output-.[hash].xml',
            testsuitesTitle: true,
            suiteFilename: true,
            suiteTitleSeparatedBy: '.',   // suites separator, default is space (' '), or period ('.') in jenkins mode
            jenkinsMode: true,
            toConsole: false
        },
        setupNodeEvents(on, config) {
         // ...
         const options = {
           outputRoot: config.projectRoot + '/logs/',
           specRoot: 'cypress/e2e',
           printLogsToFile: 'always',
           outputTarget: {
             'cypress-logs|txt': 'txt',
             'cypress-logs|json': 'json',
           }
         };

        require('cypress-terminal-report/src/installLogsPrinter')(on, options);

   }
  },
  });



