describe('WEB_004_006_user_enter_assignment_page_before_graceperiod_but_submission_after_grace_Period', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Teacher add assignment', () => {
  cy.importUsersViaHttp("Ustad_Teacher_and_Students.csv");
  cy.ustadClearDbAndLogin('teach1','testt1')
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains("button","Edit").click()
  cy.contains("Add block").click()
  cy.contains("Assignment").click()
  cy.get('input[id="title"]').type("Assignment 1")
  cy.get('div[data-placeholder="Description"]').type("this is a simple assignment")
  cy.contains("div","Graded").click()
  cy.contains("li","Submitted").click()
  cy.ustadSetDateTime(cy.get("#cbDeadlineDate"), new Date(Date.now())) //Deadline date and time -today
  cy.get('#cbGracePeriodDate',{timeout:5000}).should('be.visible')
  cy.ustadSetDateTime(cy.get("#cbGracePeriodDate"),  new Date(Date.now() + (2*60*1000))) // Grace period - 2 minutes after deadline
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

it('Student not able to submit assignment', () => {

  cy.ustadClearDbAndLogin('stud1','tests1')
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains('Assignment 1').click()
  cy.get('#assignment_text').get('div[contenteditable="true"]',{timeout:6000}).should('be.visible')
  cy.get('#assignment_text').click()
  cy.get('.ql-editor').ustadTypeAndVerify('Text 1')
  cy.wait(120000) //wait before submitting the assignment -2 mins-120sec
  cy.contains('SUBMIT',{timeout:5000}).click()
  cy.wait(1000) //This wait command added to make sure to the error message is visible
  cy.contains("Deadline has passed").should('exist') // error saying Deadline has passed should be visible
  cy.contains("Not submitted").should('exist') // assignment won't get submitted

})

it('Teacher checks submissions', () => {
  cy.ustadClearDbAndLogin('teach1','testt1')
 //  Assignment block
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains("button","Course").click()
  cy.contains("Assignment 1").click()
  cy.contains('Submissions').click()
  cy.ustadReloadUntilVisible("Student 1")
  cy.contains("Student 1").click()
  cy.contains("Not submitted").should('exist')
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})