const { defineConfig } = require('cypress')

module.exports = defineConfig({
  e2e: {
      //baseUrl: 'http://localhost:8087/',
      chromeWebSecurity: false
  }
})
