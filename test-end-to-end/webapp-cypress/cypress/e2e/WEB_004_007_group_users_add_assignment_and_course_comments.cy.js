describe('WEB_004_007_group_users_add_assignment_and_course_comments', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Teacher add assignment and course comment', () => {
  cy.importUsersViaHttp("Ustad_Teacher_and_Students.csv");
  cy.ustadClearDbAndLogin('teach1','testt1')
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains("button","Edit").click()
  cy.contains("Add block").click()
  cy.contains("Assignment").click()
  cy.get('input[id="title"]').clear().type("Assignment 1")
  cy.get('div[data-placeholder="Description"]').type("this is a simple assignment")
  cy.get('#group_submission_on').click()
  cy.get('#cgsName').click()
  cy.get('#add_new_groups').then(($addNewGroupsBtn) => {
    if (!$addNewGroupsBtn.is(':visible')) {
    cy.reload()
    }
    else {
    cy.get('#add_new_groups').click()
    }
    })
  cy.get('#cgs_name').type('Assignment Team')
  cy.get('#cgs_total_groups').clear().type('2')
  cy.contains('Unassigned').eq(0).click()  // s1 -G1
  cy.contains('Group 1').click()
  cy.contains('Unassigned').eq(0).click()  //s2 - G1
  cy.get('li[data-value="1"]').click()
  cy.contains('Unassigned').eq(0).click()  //s3 - G2
  cy.contains('Group 2').click()
  cy.contains('Unassigned').eq(0).click()  //s4 - G2
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
  cy.contains("button","Members").should('be.visible')
  cy.contains('Assignment 1').click()
  cy.ustadTypeAndSubmitAssignmentComment('#course_comment_textfield','#course_comment_textfield_send_button','course comment by teacher')
  cy.contains("Assignment 1").click()
  cy.contains('Submissions').click()
  cy.ustadReloadUntilVisible("Group 1")
  cy.contains("Group 1").click()
  cy.ustadTypeAndSubmitAssignmentComment('#private_comment_textfield','#private_comment_textfield_send_button','private comment by teacher')
})

it('Group 1- Student 1 submit assignment', () => {
  cy.ustadClearDbAndLogin('stud1','tests1')
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains('Assignment 1').click()
  cy.get('#assignment_text div[contenteditable="true"]',{timeout:6000}).should('be.visible')
  cy.get('#assignment_text').click()
  cy.get('.ql-editor').ustadTypeAndVerify('Text 1',{maxRetries: 3})
  cy.contains('SUBMIT',{timeout:5000}).click()
  cy.get('#assignment_text div[contenteditable="true"]').should('not.exist')
  cy.ustadTypeAndSubmitAssignmentComment('#course_comment_textfield','#course_comment_textfield_send_button','course comment by Group 1',25)
  cy.contains("course comment by Group 1").should('exist')
  cy.contains("course comment by teacher").should('exist')
  cy.contains("private comment by teacher").ustadScrollUntilVisible()
  cy.get(".VirtualList").scrollTo('bottom')
  cy.ustadTypeAndSubmitAssignmentComment('#private_comment_textfield','#private_comment_textfield_send_button','private comment by Group 1')
  cy.contains("private comment by teacher").ustadScrollUntilVisible()
  cy.contains("private comment by Group 1").should('exist')
  cy.contains("private comment by teacher").should('exist')
})

it('Group 2 Student can view  Group 1 course comment ', () => {
  cy.ustadClearDbAndLogin('stud3','tests3')
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains('Assignment 1').click()
  cy.get("#VirtualList").scrollTo('bottom')
  cy.contains("course comment by teacher").ustadScrollUntilVisible()
  cy.contains("course comment by Group 1").should('exist')
  cy.contains("course comment by teacher").should('exist')
  cy.contains("private comment by Group 1").should('not.exist')
  cy.contains("private comment by teacher").should('not.exist')
})

it('Group 1 - Student2 able to view Group 1 assignment and course comments', () => {
  cy.ustadClearDbAndLogin('stud2','tests2')
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains("button","Course").click()
  cy.contains("Assignment 1").click()
  cy.get("#VirtualList").scrollTo('bottom')
  cy.contains("Text 1").ustadScrollUntilVisible()
  cy.contains("course comment by teacher").ustadScrollUntilVisible()
  cy.contains("course comment by Group 1").should('exist')
  cy.contains("course comment by teacher").should('exist')
  cy.get("#VirtualList").scrollTo('bottom')
  cy.contains("private comment by teacher").ustadScrollUntilVisible()
  cy.contains("private comment by Group 1").should('exist')
  cy.contains("private comment by teacher").should('exist')
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})