
import './commands';
import './namespace';


// Registering a handler for test failures
Cypress.on('fail', (error, runnable) => {
  // Check if the error originated from a test (not a hook, etc.)
  if (error.message.includes('AssertionError')) {
    cy.log('Test failed! Retrieving console logs...');
    cy.window().then((win) => {
      // Get the browser console logs
      const consoleLogs = win.console.logs();

      // Log the console output for failed tests
      cy.log('Console logs:', consoleLogs);
    });
  }
});
