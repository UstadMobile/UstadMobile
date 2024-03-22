describe('WEB_007_006_admin_enable_or_disable_guest_login', () => {
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
  cy.get('#terms_html_edit .ql-editor.ql-blank').should('have.attr', 'contenteditable').and('equal', 'true',{timeout:3000})
  cy.get('#terms_html_edit .ql-editor.ql-blank').click().clear().type("CompanyTerms",{timeout:1000})
  cy.get('#guest_login_enabled').click({force:true})
  cy.get('#actionBarButton').click()
  cy.contains('Yes').should('exist')
  cy.get('#header_avatar').click()
  cy.contains('Add another account').click()
  cy.get('#connect_as_guest_button').should('exist')
  cy.contains('CONNECT AS GUEST').click()
  cy.contains('Courses').should('be.visible')
})
it('Admin disable guest login', () => {
  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.get('#settings_button').click()
  cy.contains('Site').click()
  cy.contains('Edit').click()
 //https://docs.cypress.io/api/commands/should#Assert-the-href-attribute-is-equal-to-users
  cy.get('#terms_html_edit .ql-editor').as('editor')
  //cy.get('@editor').should('have.attr', 'contenteditable').and('equal', 'true',{timeout:3000})
  cy.get('@editor').click().clear().type("null")
  cy.get('#guest_login_enabled').click({force:true})
  cy.get('#actionBarButton').click()
  cy.contains('No').should('exist')
  cy.get('#header_avatar').click()
  cy.contains('Add another account').click()
  cy.get('#connect_as_guest_button').should('not.exist')
  cy.contains('CONNECT AS GUEST').should('not.exist')
})
})