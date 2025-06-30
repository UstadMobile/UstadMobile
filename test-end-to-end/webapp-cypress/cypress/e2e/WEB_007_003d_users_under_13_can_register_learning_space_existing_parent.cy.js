describe('WEB_007_003d_users_under_13_can_register_learning_space_existing_parent', () => {
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
  //cy.contains('Join Learning Space').click()
 // cy.contains('learningspacetitle').click() // learning space name
  //cy.ustadPersonalOrLearningSpace('Learning_space')
  cy.ustadSetDate(cy.get("#age_date_of_birth"), new Date(Date.now() - (10 * 365 * 24 * 60 * 60 * 1000))) //kids age 10
  cy.contains('button','Next').click()
  cy.contains('New Terms').should('not.exist')
  cy.contains("label", "Full name*").parent().find("input").clear().type('Child User')
  cy.get('div[id="gender"]').click()
  cy.contains("li","Female").click()
  cy.contains("label", "Parent email*").parent().find("input").clear().type('parent@email.com')
  cy.contains("label", "Username").click()
  cy.get('input[value="childuser"]').should('be.visible')
  cy.contains('Next').click()
  cy.contains("label", "Password").parent().find("input").clear().type('test1234')
  cy.contains('SIGN-UP').click()
  cy.contains("Register").should('exist')
  cy.contains('button','OK').click()
  cy.contains('button[class*="MuiButton-outlinedPrimary"]', 'New user').should('exist')
})

it('Parent User register as a new user', () => {
  cy.ustadClearIndexDb()
  cy.visit('/', {timeout:60000})
  cy.contains('button[class*="MuiButton-outlinedPrimary"]', 'New user').click();
  cy.ustadSetDate(cy.get("#age_date_of_birth"), new Date(Date.now() - (30 * 365 * 24 * 60 * 60 * 1000)))
  cy.contains('button','Next').click()
  cy.contains('New Terms').should('be.visible')
  cy.get('#accept_button').click()
  cy.contains("label", "Full name*").parent().find("input").clear().type('Parent User')
  cy.get('div[id="gender"]').click()
  cy.contains("li","Female").click()
  cy.get('input[value="parentuser"]').should('be.visible')
  cy.contains('button','Next').click()
  cy.contains("label", "Password").parent().find("input").clear().type('test1234')
  cy.contains('SIGN-UP').click()
  cy.contains('Courses',{timeout:2000}).should('be.visible')
})

it('Parent user clicks on link in the email received from ustad mobile', () => {
  // Call the custom command to fetch the email and open the URL
    const email = 'parent@email.com'
    const baseUrl = '/'
  cy.UstadOpenInviteLinkFromEmail(email, baseUrl, {timeout:60000})
  cy.get("#appbar_title").contains("Select account").click()
  cy.contains('Parent User').should('exist')
  cy.contains('parentuser',{timeout:6000}).click() // Accounts screen
  cy.get("#appbar_title").contains("Manage parental consent").should("exist")
  cy.contains(' Name: Child User').should('be.visible')
  cy.get('div[aria-labelledby="undefined_label"]').contains("Mother").should("exist") //Default relation- female gender so Mother
  cy.contains("Terms and policies").should("exist")
  cy.contains('I do not consent').should("exist")
  cy.contains('I consent').click()
  cy.contains('I consent').should("not.exist")
})

it('child user able to login- consent check', () => {
  // Admin user login
  cy.ustadClearDbAndLogin('childuser','test1234',{timeout:8000})
  cy.contains('Courses',{timeout:2000}).should('be.visible')
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})