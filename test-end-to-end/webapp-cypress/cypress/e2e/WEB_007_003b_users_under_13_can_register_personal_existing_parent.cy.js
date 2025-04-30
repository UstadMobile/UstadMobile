describe('WEB_007_003b_users_under_13_can_register_using_parental_approval_link_existing_parent', () => {
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
  cy.contains('button[class*="MuiButton-outlinedPrimary"]', 'New user').click()
  cy.contains('personal').click()
  //cy.ustadPersonalOrLearningSpace('Personal')
  cy.ustadBirthDate(cy.get("#age_date_of_birth"), new Date(Date.now() - (10 * 365 * 24 * 60 * 60 * 1000))) //kids age 10
  cy.contains('button','Next').click()
  cy.contains('New Terms').should('not.exist')
  cy.contains("label", "Full name*").parent().find("input").clear().type('Child User')
  cy.get('div[id="gender"]').click()
  cy.contains("li","Female").click()
  cy.contains("label", "Parent email*").parent().find("input").clear().type('parent@email.com')
  cy.contains('button','Done').click()
  cy.contains("Wait for Parent to consent").should('exist')
})

it('Parent User register as a new user', () => {
  cy.ustadClearIndexDb()
  cy.visit('/', {timeout:60000})
  cy.contains('button[class*="MuiButton-outlinedPrimary"]', 'New user').click();
  cy.ustadBirthDate(cy.get("#age_date_of_birth"), new Date(Date.now() - (30 * 365 * 24 * 60 * 60 * 1000)))
  cy.contains('button','Next').click()
  cy.contains('New Terms').should('be.visible')
  cy.get('#accept_button').click()
  cy.contains("label", "Full name*").parent().find("input").clear().type('Parent User')
  cy.get('div[id="gender"]').click()
  cy.contains("li","Female").click()
  cy.contains('button','Next').click()
  cy.contains("label", "Username").parent().find("input").clear().type('parentuser')
  cy.contains("label", "Password").parent().find("input").clear().type('test1234')
  cy.contains('SIGN-UP').click()
  cy.contains('Courses',{timeout:2000}).should('be.visible')
})

it('Parent user clicks on link in the email received from ustad mobile', () => {
  cy.ustadClearIndexDb()
    const email = 'parent@email.com'
    const baseUrl = '/'
 // Call the custom command to fetch the email and open the URL
  cy.UstadOpenInviteLinkFromEmail(email, baseUrl, {timeout:60000})
  cy.contains('Parent User').click() // Accounts screen
  cy.contains('Child User').should('be.visible') //List of child profiles
  cy.get("#appbar_title").contains("Add child profile").should("exist")
  cy.contains('Child User').should('be.visible')
  cy.contains('Female').should('be.visible')
  // Verify DOB of child
  cy.contains('Child User').should('be.visible')
  cy.contains('button','Save').click()
  cy.contains('Courses',{timeout:2000}).should('be.visible')
})
  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})