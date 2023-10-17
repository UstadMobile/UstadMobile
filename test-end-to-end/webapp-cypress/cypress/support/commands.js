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

// Login flow
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

// Logout Flow

Cypress.Commands.add('logout', () => {
   cy.get('.MuiAvatar-colorDefault.css-154ogbs').click()
   cy.contains('LOG OUT').click()
})

// Create a new course
Cypress.Commands.add('addCourse',(courseName) => {
    cy.contains("Courses").click()
    cy.contains("button","Course").click()
    cy.contains("Add a new course").click()
    cy.get('input[id="clazz_name"]').type(courseName)
    cy.get('div[data-placeholder="Description"]').type("simple class")
    cy.contains("button","Save").click()
  })


  // Add a new person

  Cypress.Commands.add('addNewPerson',(firstName,lastName,gender) => {
        cy.contains("span","Add a new person").click()
        cy.contains("label", "First names").parent().find("input").clear().type(firstName)
        cy.contains("label", "Last name").parent().find("input").clear().type(lastName)
        cy.get('div[id="gender"]').click()
        cy.contains("li",gender).click()
        cy.contains("button","Save",{timeout: 1000}).click()
        cy.contains("button","Save",{timeout: 3000}).click()
    })

   // Create a user account
    Cypress.Commands.add('createUserAccount',(userName,password) => {
      cy.contains('Create account').click()
      cy.get('#username:not([disabled])').type(userName)
      cy.get('#newpassword').type(password)
      cy.contains("button","Save").click().should('be.visible')
      cy.contains('Change Password',{timeout:2000}).should('be.visible')
      cy.go('back')
      cy.go('back')
      })

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