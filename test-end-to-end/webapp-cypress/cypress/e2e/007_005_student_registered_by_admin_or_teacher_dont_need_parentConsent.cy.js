describe('007_004_user_registration_dob_field_is_mandatory', () => {
 it('Start Ustad Test Server ', () => {
  // Start Test Server
    cy.ustadStartTestServer()
})

it('User registration date of birth field is mandatory', () => {
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.contains("People").click()
  cy.contains("button","Person").click()
  cy.contains("label", "First names").parent().find("input").clear().type("Student")
  cy.contains("label", "Last name").parent().find("input").clear().type("1")
  cy.get('div[id="gender"]').click()
  cy.contains("li","Female").click()
  cy.ustadBirthDate(cy.get("#person_date_of_birth"),new Date(Date.now()-(365 * 24 * 60 * 60 * 1000 * 5)))
  cy.wait(2000)
  cy.contains("button","Save",{timeout: 2000}).click()
  cy.ustadCreateUserAccount('student1','test1234')
})

it('Student login successfully', () => {
 // Student user login
  cy.ustadClearDbAndLogin('student1','test1234',{timeout:8000})
  cy.contains("People").should('be.visible')
  })
})