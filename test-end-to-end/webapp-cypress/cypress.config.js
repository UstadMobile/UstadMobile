const { defineConfig } = require("cypress");

module.exports = defineConfig({
  e2e: {
    chromeWebSecurity: false,
    experimentalSessionAndOrigin: true,
    //baseUrl: 'http://localhost:8087/umapp/#/LoginView'
    reporter: 'junit',
    reporterOptions: {
    mochaFile: 'results/my-test-output.xml',
    toConsole: true
    },
  },
});
