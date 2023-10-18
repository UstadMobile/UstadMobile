
const { defineConfig } = require("cypress");

module.exports = defineConfig({
  video: true,
  e2e: {
    chromeWebSecurity: false,
    experimentalSessionAndOrigin: true,
    reporter: 'mocha-junit-reporter',
    reporterOptions: {

        testsuitesTitle: true,
        suiteTitleSeparatedBy: '.', // suites separator, default is space (' '), or period ('.') in jenkins mode
        mochaFile: 'results/my-test-output.xml',
        toConsole: true,
        jenkinsMode: true
    }

  },
});
