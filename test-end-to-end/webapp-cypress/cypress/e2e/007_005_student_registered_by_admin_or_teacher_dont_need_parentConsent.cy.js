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
  cy.contains("label", "Last name").parent().find("input").clear().type("A")
  cy.get('div[id="gender"]').click()
  cy.contains("li","Female").click()
  cy.ustadBirthDate(cy.get("#person_date_of_birth"), new Date("2017-06-01"))
  cy.contains("button","Save",{timeout: 2000}).click()
  cy.ustadCreateUserAccount('studenta','test1234')
})

it('Student login successfully', () => {
 // Student user login
  cy.ustadClearDbAndLogin('studenta','test1234',{timeout:8000})
  cy.contains("People").should('be.visible')
  })
})