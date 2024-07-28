describe('WEB_004_002_user_add_private_comment', () => {
it('Start Ustad Test Server ', () => {
 // Start Test Server
  cy.ustadStartTestServer()
})

it('Admin add a course and Members', () => {
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
 // Add a new course
  cy.ustadAddCourse('004_002')
 //Add a teacher
  cy.contains("button","Members").click()
  cy.contains("span","Add a teacher").click()
  cy.ustadAddNewPerson('Teacher','1','Female')
 // Add account for teacher
  cy.contains("Teacher 1").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('teacher1','test1234')
 //Add a student1
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','1','Male')
  cy.contains("button","Members").should('be.visible')
 //Add account for student1
  cy.contains("Student 1").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('student1','test1234')
 //Add a student2
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','2','Male')
  cy.contains("button","Members").should('be.visible')
 //Add account for student1
  cy.contains("Student 2").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('student2','test1234')
})

it('Teacher add assignment and course comment', () => {
  cy.ustadClearDbAndLogin('teacher1','test1234')
  cy.contains("Course").click()
  cy.contains("004_002").click()
  cy.contains("button","Course").click()
 // Add Assignment block
  cy.contains("button","Edit").click()
  cy.contains("Add block").click()
  cy.contains("Assignment").click()
  cy.get('input[id="title"]').type("Assignment 1")
  cy.get('div[data-placeholder="Description"]').type("this is a simple assignment")
  cy.contains("div","Graded").click()
  cy.contains("li","Submitted").click()
  cy.contains("button","Done").should('be.visible')
  cy.contains("button","Done").click()
  cy.contains("button","Save").should('be.visible')
  cy.contains("button","Save").click()
  cy.contains("button","Members").should('be.visible')
  cy.contains("Assignment 1").click()
  cy.ustadTypeAndSubmitAssignmentComment('#course_comment_textfield','#course_comment_textfield_send_button','comment1')
})

it('Student add course comment', () => {

  cy.ustadClearDbAndLogin('student1','test1234')
  cy.contains("Course").click()
  cy.contains("004_002").click()
  cy.contains("button","Course").click()
  cy.contains('Assignment 1').click()
  cy.ustadTypeAndSubmitAssignmentComment('#course_comment_textfield','#course_comment_textfield_send_button','comment2')
  cy.contains("comment1").ustadScrollUntilVisible()
})
})