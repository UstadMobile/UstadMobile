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
Cypress.Commands.add('login', (username, password) => { 
  cy.visit('http://localhost:8087/')
  //  cy.visit('/')
  cy.get('#username-input').type(username)
  cy.get('input[id="password-input"]').type(password)
  cy.get('[id="login-btn"]').click()

 })
 
Cypress.Commands.add('logout', () => { 
  cy.get('[class="MuiTypography-root MuiTypography-h5 MuiTypography-alignCenter sc-gKPRtg faYOnr css-1pakh3q"]').click()
  cy.contains('Logout').click()
 
})


Cypress.Commands.add('addContent',(fileLocation,contentName) => {
  cy.contains('Library').click()
  cy.contains('Content').click()
  cy.contains('Add file').click()
  cy.contains('Drag and drop a file here or click').click()
  cy.get('input[type=file]').selectFile((fileLocation),{force: true})
  cy.contains('Upload').click()
  cy.contains("label", "Title").parent().find("input").clear().type(contentName)
  cy.contains('Done').click()
  cy.wait(2000)
  })
  
  Cypress.Commands.add('enableGuestLoginAndRegistration',() => {
  cy.contains('Settings').click()
  cy.contains('Site').click() 
  cy.contains('edit').click() 
  cy.wait(4000) 
  cy.get('.PrivateSwitchBase-input.MuiSwitch-input.css-1m9pwf3:nth-child(1)').click({ multiple: true })
   cy.wait(4000) 
   cy.contains('Done').click() 
   cy.wait(4000) 
  })
  
  Cypress.Commands.add('guestLogin',() => {
  cy.contains('Connect as guest').click()
  })
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
