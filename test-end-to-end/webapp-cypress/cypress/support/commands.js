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

// Start Test Server
Cypress.Commands.add('ustadStartTestServer', () => {
  cy.visit('http://localhost:8075/start'); // Use cy.visit to navigate to the start page
  cy.wait(5000); // Wait for 5 seconds after visiting the start page
});


//User Login
Cypress.Commands.add('ustadClearDbAndLogin', (username, password) => {

//below command added as per : https://github.com/thisdot/open-source/blob/main/libs/cypress-indexeddb/README.md
  cy.log('Clearing IndexedDB');
  cy.clearIndexedDb('localhost_8087') // clearing index db
// Adding query parameters on the url- below command added as per - https://docs.cypress.io/api/commands/visit#Add-query-parameters
  cy.visit('http://localhost:8087/', {timeout:60000},{
    qs: {
      username,
      password,
    },

  })
  cy.contains('Login').should('be.visible')
  cy.get('input#username:not([disabled])', { timeout: 10000 }).type(username) // 10 seconds
  cy.get('input#password').type(password)
  cy.get('button#login_button').click()

})

// Logout Flow

Cypress.Commands.add('ustadLogout', () => {
   cy.get('#header_avatar').click()
   cy.contains('LOG OUT').click()
})

// Create a new course
Cypress.Commands.add('ustadAddCourse',(courseName) => {
    cy.contains("Courses").click()
    cy.contains("button","Course").click()
    cy.contains("Add a new course").click()
    cy.get('input[id="clazz_name"]').type(courseName)
    cy.get('div[data-placeholder="Description"]').type("test class")
    cy.contains("button","Save").click()
  })


  // Add a new person

  Cypress.Commands.add('ustadAddNewPerson',(firstName,lastName,gender) => {
        cy.contains("span","Add a new person").click()
        cy.contains("label", "First names").parent().find("input").clear().type(firstName)
        cy.contains("label", "Last name").parent().find("input").clear().type(lastName)
        cy.get('div[id="gender"]').click()
        cy.contains("li",gender).click()
        cy.contains("button","Save",{timeout: 2000}).click()
        cy.contains('New enrolment',{timeout: 2000}).should('be.visible')
        cy.contains("button","Save",{timeout: 2000}).click()
    })

   // Create a user account
    Cypress.Commands.add('ustadCreateUserAccount',(userName,password) => {
      cy.contains('Create account').click()
      cy.get('#username:not([disabled])').type(userName)
      cy.get('#newpassword').type(password)
      cy.contains("button","Save").click()
      cy.contains('Change Password',{timeout:2000}).should('be.visible')
      cy.go('back')
      cy.go('back')
      })

  // Add a Module Block
    Cypress.Commands.add('ustadAddModuleBlock',(moduleTitle) => {
      cy.contains("Add block").click()
      cy.contains("Module").click()
      cy.get('input[id="title"]').type(moduleTitle)
      cy.contains("button","Done").click()
      cy.contains("button","Save").click()
    })

  // Add a Text Block
    Cypress.Commands.add('ustadAddTextBlock',(textTitle) => {
      cy.contains("Add block").click()
      cy.contains("Text").click()
      cy.get('input[id="title"]').type(textTitle)
      cy.get('div[data-placeholder="Description"]').type("a simple block test")
      cy.contains("button","Done").click()

     })

  // Add a Discussion Board
    Cypress.Commands.add('ustadAddDiscussionBoard',(discussionTitle) => {
      cy.contains("Add block").click()
           cy.contains("Discussion board").click()
           cy.get('input[id="title"]').type(discussionTitle)
           cy.get('div[data-placeholder="Description"]').type("a simple discussion description")
           cy.contains("button","Done").click()

          })


  // Add Assignment block
    Cypress.Commands.add('ustadAddAssignmentBlock',(assignmentTitle,deadlineDt) => {
      cy.contains("Add block").click()
      cy.contains("Assignments").click()
      cy.get('input[id="title"]').type(assignmentTitle)
      cy.get('div[data-placeholder="Description"]').type("this is a simple assignment")
      cy.get('input[id="hide_until_date"]').click()
      cy.get('#hide_until_date')
                    .click({ multiple: true })
                    .then(input => {
                      input[0].dispatchEvent(new Event('input', { bubbles: true }))
                      input.val('2023-06-01T13:00')
                    })
                    .should('be.visible')
      /*cy.get('#hide_until_date')
                    .click({ multiple: true })
                    .then(input => {
                    input[0].dispatchEvent(new Event('input', { bubbles: true }))
                    input.val('2023-06-01T13:00')
                   })*/
    //   cy.wait(2000)
       cy.contains("div","Graded").click()
       cy.contains("li","submitted").click()
       cy.wait(2000)
       cy.get('#cbDeadlineDate')
                        .click({ multiple: true })
                        .then(input => {
                          input[0].dispatchEvent(new Event('input', { bubbles: true }))
                          input.val(deadlineDt)
                        })
                        .click({ multiple: true }).should('be.visible')
         cy.contains("button","Done").should('be.visible')
         cy.contains("button","Done").click()
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