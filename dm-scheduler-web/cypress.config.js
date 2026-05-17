const { defineConfig } = require('cypress')

module.exports = defineConfig({
  e2e: {
    baseUrl: 'http://localhost:8083',
    supportFile: false,
    specPattern: 'cypress/e2e/**/*.cy.js'
  }
})

