describe('WEB_007_010_user_registration_phone_field_verification', () => {
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
 // Phone field should be having valid length according to country code (UAE- +971 : 9 digit)
  cy.get('#person_phone_num').click().clear().type('+97154402147')
  cy.contains("label", "Username*").parent().find("input").clear().type('studentc')
  cy.contains("label", "Password").parent().find("input").clear().type('test1234')
  cy.contains('Register').click()
  cy.get('.Mui-error').contains('Phone').should('exist') //phone error
  cy.contains('Invalid').should('be.visible')
    cy.get('#person_phone_num').click().clear().type('+971544021476')
  cy.contains('Register').click()
  cy.contains('Courses').should('be.visible')
})
})