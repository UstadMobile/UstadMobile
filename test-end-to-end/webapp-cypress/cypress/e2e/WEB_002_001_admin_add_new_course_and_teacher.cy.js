describe('WEB_002_001_admin_add_new_course_and_teacher ', () => {
  before(() => {
     // Start Test Server
     cy.ustadStartTestServer(6000)
  })

it('Admin user create a course and add members to the course', () => {
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.contains("Courses").should('be.visible')  // Assertion to check the user logged in successfully
 // Add a new course
  cy.ustadAddCourse('002_001')
 //Add a teacher
  cy.contains("button","Members").click()
  cy.contains("span","Add a teacher").click()
  cy.ustadAddNewPerson('Teacher','1','Female')
 //Add a student
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','1','Male')
  cy.contains("button","Members").should('be.visible')
 // Add account for teacher
  cy.contains("Teacher 1").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('teach1','testt1')
 //Add account for student
  cy.contains("Student 1").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('stud1','tests1')

})

it('Teacher able to login to the app', () => {
 // Teacher Login
  cy.ustadClearDbAndLogin('teach1','testt1')
  cy.contains("Courses").should('be.visible')  // Assertion to check the user logged in successfully
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})