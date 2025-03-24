describe('WEB_007_007_admin_or_teacher_send_group_invitation_to_join_course_001_username_invite', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Tearcher invite student to the course via contacts-username', () => {
 // Teacher Login
  cy.importUsersViaHttp("Ustad_Unenrolled_Teacher_And__Student_list.csv");
  cy.ustadClearDbAndLogin('teach1','testt1')
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains("Course").click()
  cy.contains("Members").click()
  cy.contains("Add a student").click()
  cy.contains("Invite Via Contact").click()
  cy.get("input[role='combobox']").click().type("@stud1 , @stud2")
  cy.contains("Send").click()
  cy.contains("@stud1").should('exist')
  cy.contains("@stud2").should('exist')
})

it('Student able to accept course invite', () => {
  cy.ustadClearDbAndLogin('stud1','tests1')
  cy.contains("Messages").click()
  cy.contains("Teacher 1").click()
  cy.get('.MuiBox-root.css-dwtjoi a').click()
  cy.contains("Do you want to join this course?").should("exist")
  cy.contains("ACCEPT").click()
  cy.contains("Test Course Block",{timeout: 10000}).should("exist")
})
it('Tearcher verify student added to the course', () => {
 // Teacher Login
  cy.ustadClearDbAndLogin('teach1','testt1')
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains("Course").click()
  cy.contains("Members").click()
  cy.contains("Student 1").should('be.visible')
})
})