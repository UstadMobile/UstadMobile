import setDate from '../support/setDate'; //https://github.com/cypress-io/cypress/issues/1366#issuecomment-437878862
describe('004_001_assignment_creation_submission_grading', () => {
it('Start Ustad Test Server ', () => {
 // Start Test Server
  cy.ustadStartTestServer()
})

it('Admin add a course and assignment block', () => {
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
 // Add a new course
  cy.ustadAddCourse('004_001')
 //Add a teacher
  cy.contains("button","members").click()
  cy.contains("span","Add a teacher").click()
  cy.ustadAddNewPerson('Teacher','1','Female')
 // Add account for teacher
  cy.contains("Teacher 1").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('teacher1','test1234')
 //Add a student1
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','1','Male')
  cy.contains("button","members").should('be.visible')
 //Add account for student1
  cy.contains("Student 1").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('student1','test1234')
})

it('Teacher add assignment', () => {
  cy.ustadClearDbAndLogin('teacher1','test1234')
  // Add Assignment block
  cy.contains("Course").click()
  cy.contains("004_001").click()
  cy.contains("button","members").click() // This is a temporary command to make sure member list is loaded
  cy.contains("button","Course").click()
  cy.contains("button","Edit").click()
  cy.contains("Add block").click()
  cy.contains("Assignment").click()
  cy.get('input[id="title"]').type("Assignment 1")
  cy.get('div[data-placeholder="Description"]').type("this is a simple assignment")
 /* when grace period is added - student not able to see the assignment
  cy.get('input[id="hide_until_date"]').click()
  cy.get('#hide_until_date')
    .click()
    .then(input => setDate(input[0], '2023-11-08T00:00'))
 */
  cy.contains("div","Graded").click()
  cy.contains("li","submitted").click()
  cy.get('#cbDeadlineDate')
    .then(input => setDate(input[0], '2023-12-20T00:00'))
  cy.get('#caSubmissionPolicy').click()
  cy.contains("button","Done").should('be.visible')
  cy.contains("button","Done").click()
  cy.contains("button","Save").should('be.visible')
  cy.contains("button","Save").click()
  cy.contains("button","members").should('be.visible')
  cy.contains("button","Edit").click()
  cy.contains("Assignment 1").click()
  cy.contains("button","Done").should('be.visible')
  cy.contains("button","Done").click()
  cy.contains("button","Save").should('be.visible')
  cy.contains("button","Save").click()
  cy.contains("button","members").should('be.visible')
})

it('Student submit assignment', () => {

  cy.ustadClearDbAndLogin('student1','test1234')
  cy.contains("Course").click()
  cy.contains("004_001").click()
  cy.contains('Assignment 1').click()
  cy.get('#assignment_text').get('div[contenteditable="true"]',{timeout:6000}).should('be.visible')
  cy.get('#assignment_text').click()
  cy.get('#assignment_text').type("Text 1")
  cy.contains('SUBMIT',{timeout:5000}).click()
  cy.go('back')
  cy.contains('Assignment 1',{timeout:1000}).click()
  cy.contains("Not submitted").should('not.exist')
})

it('Teacher add assignment mark and course comment', () => {
  cy.ustadClearDbAndLogin('teacher1','test1234')

 //  Assignment block
  cy.contains("Course").click()
  cy.contains("004_001").click()
  cy.contains("button","members").click()  // This is a temporary command to make sure member list is loaded
  cy.contains("button","Course").click()
  cy.contains("Assignment 1").click()
  cy.contains('Submissions').click()
  cy.contains('Student 1').click()
  cy.contains("Text 1").should('be.visible')
  cy.get('#marker_comment').type("Keep it up")
  cy.get('#marker_mark').type('9')
  cy.get('#submit_mark_button').click()
  cy.contains('Keep it up').should('exist')
  cy.contains('9/10 Points').should('exist')
})

it('Student view his grade', () => {
  cy.ustadClearDbAndLogin('student1','test1234')
  cy.contains("Course").click()
  cy.contains("004_001").click()
  cy.contains('Assignment 1').click()
  cy.contains('Keep it up').should('exist')
  cy.contains('9/10 Points').should('exist')
  cy.contains('SUBMIT').should('not.exist') // assertion to make sure multiple submission is not allowed
  })
})