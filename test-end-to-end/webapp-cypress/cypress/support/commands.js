// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add('login', (email, password) => { ... })
//below command added as per : https://github.com/thisdot/open-source/blob/main/libs/cypress-indexeddb/README.md
import '@this-dot/cypress-indexeddb';

Cypress.Commands.add('login', (username, password) => {
  cy.clearIndexedDb('localhost_8087');
  cy.visit('http://localhost:8087/', {
    qs: {
      username,
      password,
    },

  });

  // Assuming that elements are found by their IDs
  cy.get('input#username', { timeout: 6000 }).should('be.visible').type(username); // 5 seconds
  cy.get('input#password').type(password);
  cy.get('button#login_button').click();
});


//commands.js
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })