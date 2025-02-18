describe('WEB_004_008_group_users_single_assignment_submission_allowed', () => {
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
  cy.get('input[id="title"]').clear().type("Assignment 1")
  cy.get('div[data-placeholder="Description"]').type("this is a simple assignment")
  cy.contains('Must submit all at once').should('exist')
  cy.get('#group_submission_on').click()
  cy.get('#cgsName').click()
  cy.contains('Add new groups', { timeout: 5000 }).then(($addNewGroupsBtn) => {
     if (!$addNewGroupsBtn.is('not.visible')) {
     cy.reload()
     }
     })
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
  cy.contains('Unassigned').eq(0).click()  //s5 - G1
  cy.get('li[data-value="1"]').click()
  cy.contains('Unassigned').eq(0).click()  //s6 - G2
  cy.get('li[data-value="2"]').click()
  cy.contains("button","Save").should('be.visible')
  cy.contains("button","Save").click()
  cy.get('input[id="title"]').clear().type("Assignment 1")
  cy.contains("button","Done").should('be.visible')
  cy.contains("button","Done").click()
  cy.contains("button","Save").should('be.visible')
  cy.contains("button","Save").click()
  cy.wait(1000) // This command helps to view Assignment to user
  cy.contains("button","Members",{timeout:2000}).should('be.visible')
})

it('Group 1- Student 1 submit assignment', () => {
  cy.ustadClearDbAndLogin('stud1','tests1')
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains('Assignment 1').click()
  cy.get('#assignment_text div[contenteditable="true"]',{timeout:6000}).should('be.visible')
  cy.get('#assignment_text').click()
  cy.get('.ql-editor').ustadTypeAndVerify('Text 1')
  cy.contains('SUBMIT').click()
  cy.get('#assignment_text div[contenteditable="true"]',{timeout:6000}).should('not.exist')
  cy.contains("Not submitted").should('not.exist')
  cy.go('back')
  cy.contains('Assignment 1',{timeout:1000}).click()
  cy.contains("Not submitted").should('not.exist')
})

it('Group 1 - Student2 able to view Group 1 assignment and submit button not visible since it is single submission', () => {
  cy.ustadClearDbAndLogin('stud2','tests2')
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains("button","Course").click()
  cy.contains("Assignment 1").click()
  cy.get(".VirtualList").scrollTo('bottom')
  cy.contains("Text 1").should('be.visible')
  cy.contains("SUBMIT").should('not.exist')
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})