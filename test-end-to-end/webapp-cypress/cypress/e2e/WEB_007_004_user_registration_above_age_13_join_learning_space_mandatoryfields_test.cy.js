describe('WEB_007_004_user_registration_above_age_13_join_learning_space_mandatoryfields_test', () => {
 it('Start Ustad Test Server ', () => {
  // Start Test Server
    cy.ustadStartTestServer()
})

it('Admin enable registration', () => {
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.ustadEnableUserRegistration()
  })

it('Admin enable registration', () => {
  cy.log('Clearing IndexedDB');
  cy.clearIndexedDb('localhost_8087') // clearing index db
  cy.visit('http://localhost:8087/', {timeout:60000})
  cy.contains('button[class*="MuiButton-outlinedPrimary"]', 'New user').click()
  cy.get("#age_date_of_birth").should('be.visible')
  cy.contains('button','Next').click()
  cy.contains('This field is required').should('be.visible') //verify the DOB field is mandatory
  cy.ustadBirthDate(cy.get("#age_date_of_birth"), new Date("2010-06-01"))
  cy.contains('button','Next').click()
  cy.contains('New Terms').should('be.visible')
  cy.get('#accept_button').click()
  cy.contains("label", "Full name*").should('be.visible')
  cy.contains('button','Next').click()
  cy.contains('This field is required').should('be.visible')
  cy.get('.Mui-error').contains('Full name*').should('exist') //verify the Full name field's mandatory
  cy.get('.Mui-error').contains('Gender*').should('exist') //verify the Gender field's mandatory
  cy.contains("label", "Full name*").parent().find("input").clear().type('New User')
  cy.get('div[id="gender"]').click()
  cy.contains("li","Female").click()
  cy.contains('button','Next').click()
  cy.contains("label", "Username").should('be.visible')
  cy.contains('SIGN-UP').click()
  cy.contains('This field is required').should('be.visible')
  cy.get('.Mui-error').contains('Username').should('exist') //verify the Username field's mandatory
  cy.get('.Mui-error').contains('Password').should('exist') //verify the Password field's mandatory
  cy.contains("label", "Username").parent().find("input").clear().type('newuser')
  cy.contains("label", "Password").parent().find("input").clear().type('test1234')
  cy.contains('SIGN-UP').click()
  cy.contains('Courses',{timeout:2000}).should('be.visible')
})
})