describe('WEB_007_002_user_registration_above_age_13', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Admin enable registration', () => {
  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.ustadEnableUserRegistration()
  cy.get('#header_avatar').click()
  cy.contains('Add another account').click()
  cy.get('#create_account_button').should('be.visible')
  cy.get('#create_account_button').click()
// Verify Date of birth field is mandatory
  cy.contains('button','Next').click()
  cy.get('.Mui-error').contains('Birthday*').should('exist')
  cy.ustadSetDate(cy.get("#age_date_of_birth"), new Date("2010-06-01"));
  cy.contains('button','Next').click()
  cy.contains('New Terms').should('be.visible')
  cy.get('#accept_button').click()
  cy.contains('Register').click()
  cy.get('.Mui-error').contains('First names').should('exist') //Firstnames error
  cy.get('.Mui-error').contains('Last name').should('exist') //Lastname error
  cy.get('.Mui-error').contains('Gender').should('exist') //gender error
  cy.get('.Mui-error').contains('Username').should('exist') //username error
  cy.get('.Mui-error').contains('Password').should('exist') //password error
 // verify password field as blank
  cy.contains("label", "First names").parent().find("input").clear().type('student')
  cy.contains("label", "Last name").parent().find("input").clear().type('1')
  cy.get('div[id="gender"]').click()
  cy.contains("li","Female").click()
  cy.contains("label", "Username*").parent().find("input").clear().type('student1')
  cy.contains('Register').click()
  cy.get('.Mui-error').contains('Password').should('exist')
  cy.contains('This field is required').should('be.visible')
 // verify username field as blank
  cy.contains("label", "Username*").parent().find("input").clear()
  cy.contains("label", "Password").parent().find("input").clear().type('test1234')
  cy.contains('Register').click()
  cy.get('.Mui-error').contains('Username').should('exist') //username error
 // verify firstname field as blank
  cy.contains("label", "Username*").parent().find("input").clear().type('student1')
  cy.contains("label", "First names").parent().find("input").clear()
  cy.contains('Register').click()
 // verify lastname field as blank
  cy.contains("label", "First names").parent().find("input").clear().type('student')
  cy.contains("label", "Last name").parent().find("input").clear()
  cy.contains('Register').click()
  cy.get('.Mui-error').contains('Last name').should('exist') //Lastname error
  cy.contains("label", "Last name").parent().find("input").clear().type('1')
  cy.get('div[id="gender"]').click()
  cy.contains("li","Female").click()
 // Email error  [white space, [, ],/ ] is prohibited for email
  cy.get('#person_email_addr').click().clear().type('tester@gma il')
  cy.contains('Register').click()
  cy.get('.Mui-error').contains('Email').should('exist') //Email error
  cy.contains('Invalid').should('be.visible')
  cy.get('#person_email_addr').click().clear().type('tester@gmail.com')
// Phone field should be having valid length according to country code (UAE- +971 : 9 digit)
  cy.get('#person_phone_num').click().clear().type('+97154402147')
  cy.contains('Register').click()
  cy.get('.Mui-error').contains('Phone').should('exist') //phone error
  cy.contains('Invalid').should('be.visible')
  cy.get('#person_phone_num').click().clear().type('+971544021476')
  cy.contains('Register').click()
  cy.contains('Courses').should('be.visible')
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})