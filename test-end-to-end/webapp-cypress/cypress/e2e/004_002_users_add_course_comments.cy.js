import setDate from '../support/setDate'; //https://github.com/cypress-io/cypress/issues/1366#issuecomment-437878862
describe('004_002_user_add_private_comment', () => {
it('Start Ustad Test Server ', () => {
 // Start Test Server
  cy.ustadStartTestServer()
})

it('Admin add a course and members', () => {
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
 // Add a new course
  cy.ustadAddCourse('004_002')
 // Add module block
 // cy.contains('button','Edit').click()
 // cy.ustadAddModuleBlock('module 1')
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
 //Add a student2
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','2','Male')
  cy.contains("button","members").should('be.visible')
 //Add account for student1
  cy.contains("Student 2").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('student2','test1234')
})

it('Teacher add assignment and course comment', () => {
  cy.ustadClearDbAndLogin('teacher1','test1234')
  cy.contains("Course").click()
  cy.contains("004_002").click()
  cy.contains("button","members").click()  // This is a temporary command to make sure member list is loaded
  cy.contains("button","Course").click()
 // Add Assignment block
  cy.contains("button","Edit").click()
  cy.contains("Add block").click()
  cy.contains("Assignment").click()
  cy.get('input[id="title"]').type("Assignment 1")
  cy.get('div[data-placeholder="Description"]').type("this is a simple assignment")
 /* cy.get('input[id="hide_until_date"]').click()
  cy.get('#hide_until_date')
    .click({ multiple: true })
    .then(input => setDate(input[0], '2023-11-01T00:00'));
  cy.contains("div","Graded").click()
  cy.contains("li","submitted").click()
  cy.get('#cbDeadlineDate')
    .then(input => setDate(input[0], '2023-12-20T00:00'));*/
  cy.contains("button","Done").should('be.visible')
  cy.contains("button","Done").click()
  cy.contains("button","Save").should('be.visible')
  cy.contains("button","Save").click()
  cy.contains("button","members").should('be.visible')
  cy.contains("Assignment 1").click()
  cy.get('#course_comment_textfield').click()
  cy.get('#course_comment_textfield').type("comment1",{timeout:5000})
  cy.get('svg[data-testid="SendIcon"]',{timeout:5000}).click()
  cy.contains("comment1").should('exist')

})

it('Student add course comment', () => {

  cy.ustadClearDbAndLogin('student1','test1234')
  cy.contains("Course").click()
  cy.contains("004_002").click()
  cy.contains("button","members").click()  // This is a temporary command to make sure member list is loaded
  cy.contains("button","Course").click()
  cy.contains('Assignment 1').click()
  cy.get('#course_comment_textfield').click()
  cy.get('#course_comment_textfield').type("comment2")
  cy.get('svg[data-testid="SendIcon"]',{timeout:2000}).click()
  cy.contains("comment2").should('exist')
  cy.contains("comment1").should('exist')
})
})