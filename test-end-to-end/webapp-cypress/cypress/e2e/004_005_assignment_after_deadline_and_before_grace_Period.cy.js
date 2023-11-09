import setDate from '../support/setDate'; //https://github.com/cypress-io/cypress/issues/1366#issuecomment-437878862
describe('004_005_assignment_after_deadline_and_before_grace_Period', () => {
it('Start Ustad Test Server ', () => {
 // Start Test Server
  cy.ustadStartTestServer()
})

it('Admin add a course and assignment block', () => {
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
 // Add a new course
  cy.ustadAddCourse('004_005')
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
  cy.contains("004_005").click()
  cy.contains("button","Edit").click()
  cy.contains("Add block").click()
  cy.contains("Assignment").click()
  cy.get('input[id="title"]').type("Assignment 1")
  cy.get('div[data-placeholder="Description"]').type("this is a simple assignment")
  //cy.get('input[id="hide_until_date"]').click()
  //cy.get('#hide_until_date')
   // .click()
   // .then(input => setDate(input[0], '2023-11-08T00:00'));
  //cy.debug();
  cy.get('#cbDeadlineDate')
    .then(input => setDate(input[0], '2023-11-05T00:00'))
  cy.contains("div","Graded").click()
  cy.contains("li","submitted").click()
  cy.get('#cbGracePeriodDate',{timeout:5000}).should('be.visible')
  cy.get('#cbGracePeriodDate',{timeout:2000})
    .then(input => setDate(input[0], '2023-11-12T00:00'))
  cy.get('#caSubmissionPolicy').click()
  cy.contains('Can make multiple submissions').click()
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
  cy.contains("004_005").click()
  cy.contains('Assignment 1').click()
  cy.get('#assignment_text').click()
  cy.get('#assignment_text').type("Assignment submitted")
  cy.contains('SUBMIT').click()
  cy.contains("Assignment submitted").should('be.visible')
})

it('Teacher add marks', () => {
  cy.ustadClearDbAndLogin('teacher1','test1234')
 // Adding attendance
  cy.contains('004_004').click()
  cy.contains("button","Attendance").click()
  cy.contains("button","Record attendance").click()
  cy.contains("button","Next").click()
  cy.contains('Student 1').should('be.visible')
  cy.contains('Mark all present').click()
  cy.contains("button","Save").should('be.visible')
  cy.contains("button","Save").click()
  cy.contains('1 Present, 0 Partial, 0 Absent').should('be.visible')
 //  Assignment block
  cy.contains("Course").click()
  cy.contains("004_004").click()
  cy.contains("Assignment 1").click()
  cy.contains('Submissions').click()
  cy.contains('Student 1').click()
  cy.get('#marker_comment').type("Keep it up")
  cy.get('#marker_mark').type('9')
  cy.get('#submit_mark_button').click()
  cy.contains('Keep it up').should('be.visible')
  cy.contains('9/10 Points').should('be.visible')
})
/*
it('Student add assignment', () => {
  cy.ustadClearDbAndLogin('student1','test1234')
  cy.contains("Course").click()
  cy.contains("004_004").click()
  cy.wait(6000)
  cy.contains('Assignment 1').click()
  cy.contains('Keep it up').should('be.visible')
  cy.contains('9/10 Points').should('be.visible')
})*/
})