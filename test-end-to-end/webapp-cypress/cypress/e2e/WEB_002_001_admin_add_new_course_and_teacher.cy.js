describe('WEB_002_001_admin_add_new_course_and_teacher ', () => {
 it('Start Ustad Test Server ', () => {
 // Start Test Server
  cy.ustadStartTestServer()
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
  cy.ustadAddNewPerson('Teacher','002_001','Female')
 //Add a student
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','002_001','Male')
  cy.contains("button","Members").should('be.visible')
 // Add account for teacher
  cy.contains("Teacher 002_001").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('teacher21','test1234')
 //Add account for student
  cy.contains("Student 002_001").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('student21','test1234')

})

it('Teacher able to login to the app', () => {
 // Teacher Login
  cy.ustadClearDbAndLogin('teacher21','test1234')
  cy.contains("Courses").should('be.visible')  // Assertion to check the user logged in successfully
})
})