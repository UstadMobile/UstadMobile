import setDate from '../support/setDate'; //https://github.com/cypress-io/cypress/issues/1366#issuecomment-437878862
describe('004_008_group_users_add_assignment_and_course_comments', () => {
it('Start Ustad Test Server ', () => {
 // Start Test Server
  cy.ustadStartTestServer()
})

it('Admin add a course and members', () => {
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
 // Add a new course
  cy.ustadAddCourse('004_011')
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
 //Add a student3
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','3','Male')
  cy.contains("button","members").should('be.visible')
 //Add account for student3
  cy.contains("Student 3").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('student3','test1234')
 //Add a student4
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','4','Male')
  cy.contains("button","members").should('be.visible')
 //Add account for student4
  cy.contains("Student 4").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('student4','test1234')
})

it('Teacher add multiple submission assignment and group ', () => {
  cy.ustadClearDbAndLogin('teacher1','test1234')
 // Add Assignment block
  cy.contains("Courses").click()
  cy.contains("004_011").click()
  cy.contains("button","members").click()  // This is a temporary command to make sure member list is loaded
  cy.contains("button","Course").click()
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
  cy.get('#caSubmissionPolicy').click()
  cy.contains('Can make multiple submissions').click()
  cy.get('#cgsName').click()
  cy.wait(5000) // added to load "Add new groups" button
  cy.contains('Add new groups',{timeout: 5000}).click()
  cy.get('#cgs_name').type('Assignment Team')
  cy.get('#cgs_total_groups').clear().type('2')
  cy.contains('Unassigned').eq(0).click()  // s1
  cy.contains('Group 1').click()
  cy.contains('Unassigned').eq(0).click()  //s2
  cy.get('li[data-value="1"]').click()
  cy.contains('Unassigned').eq(0).click()  //s3
  cy.contains('Group 2').click()
  cy.contains('Unassigned').eq(0).click()  //s4
  cy.get('li[data-value="2"]').click()
  cy.contains("button","Save").should('be.visible')
  cy.contains("button","Save").click()
  cy.get('input[id="title"]').type("Assignment 1")
  cy.contains("button","Done").should('be.visible')
  cy.contains("button","Done").click()
  cy.contains("button","Save").should('be.visible')
  cy.contains("button","Save").click()
  cy.contains("button","members").should('be.visible')

})

it('Group 1- Student 1 submit assignment', () => {

  cy.ustadClearDbAndLogin('student1','test1234')
  cy.contains("Course").click()
  cy.contains("004_011").click()
  cy.contains('Assignment 1').click()
  cy.get('#assignment_text').get('div[contenteditable="true"]',{timeout:6000}).should('be.visible')
  cy.get('#assignment_text').click()
  cy.get('#assignment_text').type("Text 1")
  cy.contains('SUBMIT',{timeout:5000}).click()
  cy.go('back')
  cy.contains('Assignment 1',{timeout:1000}).click()
  cy.contains("Not submitted").should('not.exist')
})

it('Group 1 - Student2 able to view Group 1 assignment and submit button not visible since it is single submission', () => {

    cy.ustadClearDbAndLogin('student2','test1234')

   //  Assignment block
    cy.contains("Course").click()
    cy.contains("004_011").click()
    cy.contains("button","members").click()  // This is a temporary command to make sure member list is loaded
    cy.contains("button","Course").click()
    cy.contains("Assignment 1").click()
    cy.contains("Text 1").should('be.visible')
    cy.contains("SUBMIT").should('exist')
    cy.get('#assignment_text').get('div[contenteditable="true"]',{timeout:6000}).should('be.visible')
    cy.get('#assignment_text').click()
    cy.get('#assignment_text').type("Text 1")
    cy.contains('SUBMIT',{timeout:5000}).click()
    cy.go('back')
    cy.contains('Assignment 1',{timeout:1000}).click()
    cy.contains("Not submitted").should('not.exist')

})
})