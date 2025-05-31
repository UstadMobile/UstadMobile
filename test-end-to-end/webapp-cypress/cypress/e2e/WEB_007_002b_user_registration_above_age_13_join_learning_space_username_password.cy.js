describe('WEB_007_002b_user_registration_above_age_13_join_learning_space_username_password', () => {
 before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Admin enable registration', () => {
  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.ustadEnableUserRegistration()
  })

it('User age above 13 register as a new user', () => {
  cy.ustadClearIndexDb()
  cy.visit('/', {timeout:60000})
  cy.contains('button[class*="MuiButton-outlinedPrimary"]', 'New user').click();
  cy.ustadBirthDate(cy.get("#age_date_of_birth"), new Date("2010-06-01"));
  cy.contains('button','Next').click()
  cy.contains('New Terms').should('be.visible')
  cy.get('#accept_button').click()
  cy.contains("label", "Full name*").parent().find("input").clear().type('New User')
  cy.get('div[id="gender"]').click()
  cy.contains("li","Female").click()
  cy.contains("* Required").click()
  cy.get("input[value='newuser']").should('exist')
  cy.contains('button','Next').click()
  cy.get("input[id='password']").type('test1234')
  cy.contains('SIGN-UP').click()
  cy.contains('Courses',{timeout:2000}).should('be.visible')
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})