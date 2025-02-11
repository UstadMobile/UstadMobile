describe('WEB_004_011_peer_marking_for_group_assignment', () => {
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
  cy.get('div[data-placeholder="Description"]').type("this is a simple assignment")
  cy.contains('Must submit all at once').should('exist')
  cy.get('#group_submission_on').click()
  cy.get('#cgsName').click()
  cy.contains('Add new groups').then(($addNewGroupsBtn) => {
    if (!$addNewGroupsBtn.is(':visible')) {
    cy.reload()
    }
    else {
    cy.get('#add_new_groups').click()
    }
    })
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
  cy.get('input[id="title"]').type("Assignment 1")
  cy.get('#caMarkingType').click()
  cy.contains("li","Peers").click()
  cy.get('#caPeerReviewerCount').should('exist')
  cy.get('#caPeerReviewerCount').type('1')
  cy.wait(1000) // wait to load the group list -tests getting failed without this command
  cy.get('#buttonAssignReviewers').click()
  cy.contains('Group 1').should('be.visible',{timeout:8000})
  cy.contains('Unassigned').eq(0).click()
  cy.get('li[role="option"]').eq(1).should('be.visible')
  cy.get('li[role="option"]').eq(1).click()
  cy.contains('Unassigned').eq(0).click()
  cy.get('li[role="option"]').eq(1).should('be.visible')
  cy.get('li[role="option"]').eq(1).click()

  //Click done on peer allocation
  cy.contains("button","Done").should('be.visible')
  cy.contains("button","Done").click()

  //Click done on assignment edit
  cy.url().should('include', "CourseAssignmentEdit") //Ensure that navigation to assignment edit done
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

it('Group 1- Student 1 submit assignment', () => {
  cy.ustadClearDbAndLogin('stud1','tests1')
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains('Assignment 1').click()
  cy.get('#assignment_text').get('div[contenteditable="true"]',{timeout:6000}).should('be.visible')
  cy.get('#assignment_text').click()
  cy.get('.ql-editor').ustadTypeAndVerify('Text 1')
  cy.contains('SUBMIT',{timeout:5000}).click()
  cy.contains("Not submitted").should('not.exist')
  cy.contains("Submission 1").should("be.visible")
  cy.contains("Text 1").should("be.visible")
  cy.go('back')
  cy.contains('Assignment 1',{timeout:1000}).click()
  cy.contains("Not submitted").should('not.exist')
})


it('Student3 add assignment mark for Group 1', () => {
  cy.ustadClearDbAndLogin('stud3','tests3')
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains("button","Course").click()
  cy.contains("Assignment 1").click()
  cy.contains('Peers to review').click()
  cy.ustadReloadUntilVisible("Group 1")
  cy.contains("Group 1").click()
  cy.contains("Text 1").should('be.visible')
  cy.get('#marker_comment').type("Keep it up")
  cy.get('#marker_mark').type('9')
  cy.get('#submit_mark_button').click()
  cy.contains('Keep it up').should('exist')
  cy.contains('9/10 Points').should('exist')
})

it('Student2 -Group 1 view his grade', () => {
  cy.ustadClearDbAndLogin('stud2','tests2')
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