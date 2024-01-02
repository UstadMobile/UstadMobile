describe('007_006_admin_enable_or_disable_guest_login', () => {
 it('Start Ustad Test Server ', () => {
  // Start Test Server
    cy.ustadStartTestServer()
})
  it('Admin enable guest login', () => {

  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.get('#settings_button').click()
  cy.contains('Site').click()
  cy.contains('Edit').click()
 //https://docs.cypress.io/api/commands/should#Assert-the-href-attribute-is-equal-to-users
  cy.get('.ql-editor.ql-blank').should('have.attr', 'contenteditable').and('equal', 'true',{timeout:3000})
  cy.get('.ql-editor.ql-blank').click()
  cy.get('.ql-editor.ql-blank').type("CompanyTerms",{timeout:1000})
  cy.get('#guest_login_enabled').click({force:true})
  cy.get('#actionBarButton').click()
  cy.contains('Yes').should('exist')
  cy.get('#header_avatar').click()
  cy.contains('Add another account').click()
  cy.get('#connect_as_guest_button').should('exist')
})
it('Admin disable registration', () => {
  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.get('#settings_button').click()
  cy.contains('Site').click()
  cy.contains('Edit').click()
 //https://docs.cypress.io/api/commands/should#Assert-the-href-attribute-is-equal-to-users
  cy.get('.ql-editor.ql-blank').should('have.attr', 'contenteditable').and('equal', 'true',{timeout:3000})
  cy.get('.ql-editor.ql-blank').type("CompanyTerms")
  cy.get('#guest_login_enabled').click({force:true})
  cy.get('#actionBarButton').click()
  cy.contains('No').should('exist')
  cy.get('#header_avatar').click()
  cy.contains('Add another account').click()
  cy.get('#connect_as_guest_button').should('not.exist')
})
})