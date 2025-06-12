describe('WEB_007_001_admin_enable_or_disable_user_registration_and_manage_bottom_navigation_bar', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Admin enable registration', () => {
  cy.importUsersViaHttp("Ustad_Teacher_and_Students.csv");
  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.ustadEnableUserRegistration()
})

it('Verify New user registration is enabled and mandatory fields are filled', () => {
  cy.ustadClearIndexDb()
  cy.visit('/', {timeout:60000})
  cy.contains('button[class*="MuiButton-outlinedPrimary"]', 'New user').click()
  cy.get("#age_date_of_birth").should('be.visible')
  cy.contains('button','Next').click()
  cy.contains('This field is required').should('be.visible') //verify the DOB field is mandatory
  cy.ustadSetDate(cy.get("#age_date_of_birth"), new Date(new Date().setFullYear(new Date().getFullYear() - 15))) // Set date to 15 years ago
  cy.contains('button','Next').click()
  cy.contains('New Terms').should('be.visible')
  cy.get('#accept_button').click()
  cy.contains("label", "Full name*").should('be.visible')
  cy.contains('button','Next').click()
  cy.contains('This field is required').should('be.visible')
  cy.get('.Mui-error').contains('Full name*').should('exist') //verify the Full name field's mandatory
  cy.get('.Mui-error').contains('Gender*').should('exist') //verify the Gender field's mandatory
  cy.get('.Mui-error').contains('Username').should('exist') //verify the Username field's mandatory
  cy.contains("label", "Full name*").parent().find("input").clear().type('New User')
  cy.get('.Mui-error').contains('Full name*').should('not.exist') //verify the Full name error is not visible once it's entered
  cy.get('div[id="gender"]').click()
  cy.contains("li","Female").click()
  cy.get("input[value='newuser']").should('exist')
  cy.contains('button','Next').click()
  cy.get("input[id='password']").should('be.visible')
  cy.contains('SIGN-UP').click()
  cy.contains('This field is required').should('be.visible')
  cy.get('.Mui-error').contains('Password').should('exist') //verify the Password field's mandatory
  cy.get("input[id='password']").type('test1234')
  cy.contains('SIGN-UP').click()
  cy.contains('Courses',{timeout:2000}).should('be.visible')
})

it('Admin disable registration', () => {
  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.get('#settings_button').click()
  cy.contains('Site').click()
  cy.contains('Edit').click()
 //https://docs.cypress.io/api/commands/should#Assert-the-href-attribute-is-equal-to-users
  cy.get('#terms_html_edit .ql-editor').as('editor')
  cy.get('@editor').click().clear()
  cy.get('.Mui-checked.PrivateSwitchBase-root', { timeout: 5000 }).should('exist') //verified registration_allowed switch is on
  cy.get('#registration_allowed').click()
  cy.get('.Mui-checked.PrivateSwitchBase-root', { timeout: 5000 }).should('not.exist') //verified registration_allowed switch is off
  cy.get('#actionBarButton').click()
  cy.contains('Yes', { timeout: 5000 }).should('not.exist')
  cy.contains('No', { timeout: 10000 }).should('exist')
})

it('Verify New user registration is disabled', () => {
  cy.ustadClearIndexDb()
  cy.visit('/', {timeout:60000})
  cy.contains('button[class*="MuiButton-outlinedPrimary"]', 'New user').should('not.exist') // Verified new user registration is disabled
  cy.get('input#username', { timeout: 10000 }).should('exist')
})

it('Admin manage bottom navigation bar', () => {
  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.get('#settings_button').click()
  cy.contains('Site').click()
  cy.contains('Edit').click()
 //https://docs.cypress.io/api/commands/should#Assert-the-href-attribute-is-equal-to-users
  cy.get('#Messages').click()
  cy.get('#People').click()
  cy.get('.Mui-checked.PrivateSwitchBase-root', { timeout: 5000 }).should('not.exist') //verified registration_allowed switch is off
  cy.get('#actionBarButton').click()
  cy.contains('Courses').should('be.visible')
  cy.contains('Library').should('be.visible')
  cy.contains('Messages').should('not.be.visible')
  cy.contains('People').should('not.be.visible')
})

it('Verify student user can see bottom navigation bar changes', () => {
  cy.ustadClearDbAndLogin('stud1','tests1')
  cy.contains('Courses').should('be.visible')
  cy.contains('Library').should('be.visible')
  cy.contains('Messages').should('not.be.visible')
  cy.contains('People').should('not.be.visible')
})

it('Verify teacher can see bottom navigation bar changes', () => {
  cy.ustadClearDbAndLogin('teach1','testt1')
  cy.contains('Courses').should('be.visible')
  cy.contains('Library').should('be.visible')
  cy.contains('Messages').should('not.be.visible')
  cy.contains('People').should('not.be.visible')
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})