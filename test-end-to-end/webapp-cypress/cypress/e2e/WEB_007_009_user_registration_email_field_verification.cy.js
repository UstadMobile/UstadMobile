describe('WEB_007_009_user_registration_email_field_verification', () => {
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
  cy.ustadBirthDate(cy.get("#age_date_of_birth"), new Date("2010-06-01"));
  cy.contains('button','Next').click()
  cy.get('#accept_button').click()
  cy.contains("label", "First names").parent().find("input").clear().type('student')
  cy.contains("label", "Last name").parent().find("input").clear().type('1')
  cy.get('div[id="gender"]').click()
  cy.contains("li","Female").click()
 // Email error  @ is mandatory for email
  cy.get('#person_email_addr').click().type('tester')
  cy.contains("label", "Username*").parent().find("input").clear().type('studentc')
  cy.contains("label", "Password").parent().find("input").clear().type('test1234')
  cy.contains('Register').click()
  cy.get('.Mui-error').contains('Email').should('exist') //Email error
  cy.contains('Invalid').should('be.visible')
  // Email error  [white space, [, ],/ ] is prohibited for email
  cy.get('#person_email_addr').click().clear().type('tester@gma il')
  cy.contains('Register').click()
  cy.get('.Mui-error').contains('Email').should('exist') //Email error
  cy.contains('Invalid').should('be.visible')
  cy.get('#person_email_addr').click().clear().type('tester@gmail.com')
  cy.contains('Register').click()
  cy.contains('Courses').should('be.visible')
})
})