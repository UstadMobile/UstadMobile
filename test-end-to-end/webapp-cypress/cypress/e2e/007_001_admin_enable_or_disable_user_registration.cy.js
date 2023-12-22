describe('001_001_add_content.cy.js ', () => {
 it('Start Ustad Test Server ', () => {
  // Start Test Server
    cy.ustadStartTestServer()
})
  it('Admin user create a course and add members to the course', () => {

  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.get('#settings_button').click()
  cy.contains('Site').click()
  cy.contains('Edit').click()
  //cy.contains('Registration allowed').click()
  cy.get('input[type="checkbox"]').eq(1).click({force:'true'})
  cy.wait(5000)
  cy.get('.ql-editor.ql-blank').type("CompanyTerms")
  cy.get('#actionBarButton').click()
  cy.get('#header_avatar').click()
  cy.contains('Add another account').click()
  cy.get('#create_account_button').should('be.visible')
})
})