describe('WEB_004_002_user_add_private_comment', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })



it('Teacher add assignment and course comment', () => {
  cy.importUsersViaHttp("Ustad_Teacher_and_Students.csv");
  cy.ustadClearDbAndLogin('teach1','testt1')
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
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
  cy.ustadTypeAndSubmitAssignmentComment('#course_comment_textfield','#course_comment_textfield_send_button','course comment by teacher')
  cy.contains("Assignment 1").click()
  cy.contains('Submissions').click()
  cy.ustadReloadUntilVisible("Student 1")
  cy.contains("Student 1").click()
  cy.ustadTypeAndSubmitAssignmentComment('#private_comment_textfield','#private_comment_textfield_send_button','private comment by teacher')

})

it('Student add course comment', () => {

  cy.ustadClearDbAndLogin('stud1','tests1')
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains("button","Course").click()
  cy.contains('Assignment 1').click()
  cy.ustadTypeAndSubmitAssignmentComment('#course_comment_textfield','#course_comment_textfield_send_button','course comment by student')
  cy.contains("course comment by student").should('exist')
  cy.contains("course comment by teacher").should('exist')
  cy.contains("private comment by teacher").ustadScrollUntilVisible()
  cy.get(".VirtualList").scrollTo('bottom')
  cy.ustadTypeAndSubmitAssignmentComment('#private_comment_textfield','#private_comment_textfield_send_button','private comment by student')
  cy.contains("private comment by teacher").ustadScrollUntilVisible()
  cy.contains("private comment by student").should('exist')
  cy.contains("private comment by teacher").should('exist')


})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})