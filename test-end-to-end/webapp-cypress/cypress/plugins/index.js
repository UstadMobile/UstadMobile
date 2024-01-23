const { defineConfig } = require('cypress');
const {
  install,
  ensureBrowserFlags
} = require('@neuralegion/cypress-har-generator');

module.exports = defineConfig({
  e2e: {
    setupNodeEvents(on) {
      install(on);
      on('before:browser:launch', (browser = {}, launchOptions) => {
        ensureBrowserFlags(browser, launchOptions);
        return launchOptions;
      });
    }
  }
});