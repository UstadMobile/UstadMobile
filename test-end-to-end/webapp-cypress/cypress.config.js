const { defineConfig } = require("cypress");

module.exports = defineConfig({
  video: true,
  e2e: {
    chromeWebSecurity: false,
    experimentalSessionAndOrigin: true,
    //  reporter: 'junit',
    reporterOptions: {
     mochaFile: 'results/my-test-output.xml',
     toConsole: true
    },
  },
});
