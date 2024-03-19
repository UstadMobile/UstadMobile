describe('WEB_007_005_student_registered_by_admin_or_teacher_dont_need_parentConsent', () => {
 it('Start Ustad Test Server ', () => {
  // Start Test Server
    cy.ustadStartTestServer()
})

it('Admin add a student aged below 13', () => {
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.contains("People").click()
  cy.contains("button","Person").click()
  cy.contains("Add Person").click()
  cy.contains("label", "First names").parent().find("input").clear().type("Student")
  cy.contains("label", "Last name").parent().find("input").clear().type("1")
  cy.get('div[id="gender"]').click()
  cy.contains("li","Female").click()
 // Date now - 5 years --> student's age
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