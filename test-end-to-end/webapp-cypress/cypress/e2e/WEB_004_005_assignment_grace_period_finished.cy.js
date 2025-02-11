describe('WEB_004_005_assignment_after_deadline_and_before_grace_Period', () => {
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
  cy.ustadSetDateTime(cy.get("#cbDeadlineDate"), new Date("2023-11-01T08:30"))
  cy.contains("div","Graded").click()
  cy.contains("li","Submitted").click()
  cy.get('#cbGracePeriodDate',{timeout:5000}).should('be.visible')
  cy.ustadSetDateTime(cy.get("#cbGracePeriodDate"), new Date("2023-11-07T08:30"))
  cy.get('#caSubmissionPolicy').click()
  cy.contains('Can make multiple submissions').click()
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
  cy.contains("SUBMIT").should('not.exist')
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})