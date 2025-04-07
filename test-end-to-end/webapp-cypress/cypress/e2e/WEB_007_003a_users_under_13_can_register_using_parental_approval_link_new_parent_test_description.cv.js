describe('WEB_007_003a_users_under_13_can_register_using_parental_approval_link_new_parent_test_description', () => {
 before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Admin enable registration', () => {
  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.ustadEnableUserRegistration()
  })

it('Child user aged below 13 register as a new user', () => {
  cy.ustadClearIndexDb()
  cy.visit('/', {timeout:60000})
  cy.contains('button[class*="MuiButton-outlinedPrimary"]', 'New user').click();
  cy.ustadBirthDate(cy.get("#age_date_of_birth"), new Date("2016-06-01"));
  cy.contains('button','Next').click()
  cy.contains('New Terms').should('be.visible')
  cy.get('#accept_button').click()
  cy.contains("label", "Full name*").parent().find("input").clear().type('New User')
  cy.get('div[id="gender"]').click()
  cy.contains("li","Female").click()
  cy.contains('button','Next').click()
  cy.contains("label", "Username").parent().find("input").clear().type('newuser')
  cy.contains("label", "Password").parent().find("input").clear().type('test1234')
  cy.contains('SIGN-UP').click()
  cy.contains('Courses',{timeout:2000}).should('be.visible')
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})