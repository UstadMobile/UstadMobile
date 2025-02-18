describe('WEB_004_001_assignment_creation_submission_grading', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Teacher add assignment', () => {

  cy.importUsersViaHttp("Ustad_Teacher_and_Students.csv");
  cy.ustadClearDbAndLogin('teach1','testt1')
  // Add Assignment block
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains("button","Course").click()
  cy.contains("button","Edit").click()
  cy.contains("Add block").click()
  cy.contains("Assignment").click()
  cy.get('input[id="title"]').type("Assignment 1")
  cy.get('div[data-placeholder="Description"]').type("this is a simple assignment")
  cy.contains("div","Graded").click()
  cy.contains("li","Submitted").click()
  cy.ustadSetDateTime(cy.get("#cbDeadlineDate"),  new Date(Date.now() + (1000*60*1000)))
  cy.get('#caSubmissionPolicy').click()
  cy.contains("button","Done").should('be.visible')
  cy.contains("button","Done").click()
  cy.contains("button","Save").should('be.visible')
  cy.contains("button","Save").click()
  cy.contains("button","Members").should('be.visible')
  cy.contains("button","Edit").click()
  cy.contains("Assignment 1").click()
  cy.contains("button","Done").should('be.visible')
  cy.contains("button","Done").click()
  cy.contains("button","Save").should('be.visible')
  cy.contains("button","Save").click()
  cy.contains("button","Members").should('be.visible')
})

it('Student submit assignment', () => {

  cy.ustadClearDbAndLogin('stud1','tests1')
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains('Assignment 1').click()
  cy.get('#assignment_text').get('div[contenteditable="true"]',{timeout:6000}).should('be.visible')
  cy.get('#assignment_text').click()
  cy.get('.ql-editor').ustadTypeAndVerify('Text 1')
  cy.contains('SUBMIT').click()
  cy.get('#assignment_text div[contenteditable="true"]').should('not.exist')
  cy.contains("Not submitted").should('not.exist')
  cy.go('back')
  cy.contains('Assignment 1').click()
  cy.contains("Not submitted").should('not.exist')
})

it('Teacher add assignment mark and course comment', () => {
  cy.ustadClearDbAndLogin('teach1','testt1')

 //  Assignment block
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains("button","Course").click()
  cy.contains("Assignment 1").click()
  cy.contains('Submissions').click()
  cy.ustadReloadUntilVisible("Student 1")
  cy.contains("Student 1").click()
  cy.get('#marker_comment').type("Keep it up")
  cy.get('#marker_mark').type('9')
  cy.get('#submit_mark_button').click()
  cy.contains('Keep it up').should('exist')
  cy.contains('9/10 Points').should('exist')
//  Verify the score visible in Gradebook
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains("Gradebook").click()
  cy.contains('9').should('exist')
})

it('Student can view their grade', () => {
  cy.ustadClearDbAndLogin('stud1','tests1')
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains('Assignment 1').click()
  cy.contains('Keep it up').should('exist')
  cy.contains('9/10 Points').should('exist')
  cy.contains('SUBMIT').should('not.exist') // assertion to make sure multiple submission is not allowed
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})