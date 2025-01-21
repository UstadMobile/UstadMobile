describe('WEB_002_010_username_validation', () => {
 it('Start Ustad Test Server ', () => {
  // Start Test Server
    cy.ustadStartTestServer()
})
  it('Admin user create a person', () => {

  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.contains("Courses").should('be.visible')  // Assertion to check the user logged in successfully

 // Add a new course
  cy.contains("People").click()
  cy.contains("button","Person").click()
  cy.contains("Add Person").click()
  cy.contains("label", "First names").parent().find("input").clear().type("User")
  cy.contains("label", "Last name").parent().find("input").clear().type("1")
  cy.get('div[id="gender"]').click()
  cy.contains("li","Male").click()
  cy.contains("button","Save",{timeout: 2000}).click()
  // verify space not accepted on username field
  cy.contains("Create account").click()
  cy.get('#username:not([disabled])').type("user 1")
  cy.get('#newpassword').type("test1234")
  cy.contains("button","Save").click()
  cy.contains("Username must be 3 to 15 characters, use only letters, avoid spaces or special symbols.",{timeout:2000}).should('be.visible')
  cy.go('back')
  // verify special character not accepted on username field
  cy.contains("Create account").click()
  cy.get('#username:not([disabled])').type("user/1")
  cy.get('#newpassword').type("test1234")
  cy.contains("button","Save").click()
  cy.contains("Username must be 3 to 15 characters, use only letters, avoid spaces or special symbols.",{timeout:2000}).should('be.visible')
  cy.go('back')
  // verify special character . _ accepted on username field
  cy.contains("Create account").click()
  cy.get('#username:not([disabled])').type("user.1_2")
  cy.get('#newpassword').type("test1234")
  cy.contains("button","Save").click()
  cy.contains('Change Password',{timeout:2000}).should('be.visible')
})

it('Admin user create a person', () => {
  cy.ustadClearDbAndLogin('user 1','test1234',{timeout:8000})
  cy.contains("Username must be 3 to 15 characters, use only letters, avoid spaces or special symbols.",{timeout:2000}).should('be.visible')
  cy.reload()
  cy.get('input#username', { timeout: 10000 }).should('exist').type("user@1") // 10 seconds
  cy.get('input#password').type("test1234")
  cy.get('button#login_button').click()
  cy.contains("Username must be 3 to 15 characters, use only letters, avoid spaces or special symbols.",{timeout:2000}).should('be.visible')
  cy.reload()
  cy.get('input#username', { timeout: 10000 }).should('exist').type("user.1_2") // 10 seconds
  cy.get('input#password').type("test1234")
  cy.get('button#login_button').click()
  cy.contains("Courses").should('be.visible')
})
})