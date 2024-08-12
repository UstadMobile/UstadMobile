describe('WEB_007_004_user_registration_dob_field_is_mandatory', () => {
 it('Start Ustad Test Server ', () => {
  // Start Test Server
    cy.ustadStartTestServer()
})

it('Admin enable registration', () => {
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.ustadEnableUserRegistration()
  cy.get('#header_avatar').click()
  cy.contains('Add another account').click()
  cy.get('#create_account_button').should('be.visible')
  cy.get('#create_account_button').click()
 // Date of birth field is not selected
  cy.contains('button','Next').click()
  cy.get('.Mui-error').contains('Birthday*').should('exist')
  cy.ustadBirthDate(cy.get("#age_date_of_birth"), new Date("2010-06-01"))
  cy.contains('button','Next').click()
  cy.get('#accept_button').click()
  cy.contains("label", "First names").should('be.visible')
})
})