describe('WEB_004_013_peer_marking_for_group_assignment', () => {
it('Start Ustad Test Server ', () => {
 // Start Test Server
  cy.ustadStartTestServer()
})

it('Admin add a course and Members', () => {
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
 // Add a new course
  cy.ustadAddCourse('004_013')
 //Add a teacher
  cy.contains("button","Members").click()
  cy.contains("span","Add a teacher").click()
  cy.ustadAddNewPerson('Teacher','1','Female')
 // Add account for teacher
  cy.contains("Teacher 1").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('teacher1','test1234')
 //Add a student1
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','1','Male')
  cy.contains("button","Members").should('be.visible')
 //Add account for student1
  cy.contains("Student 1").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('student1','test1234')
 //Add a student2
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','2','Male')
  cy.contains("button","Members").should('be.visible')
 //Add account for student1
  cy.contains("Student 2").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('student2','test1234')
 //Add a student3
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','3','Male')
  cy.contains("button","Members").should('be.visible')
 //Add account for student3
  cy.contains("Student 3").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('student3','test1234')
 //Add a student4
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','4','Male')
  cy.contains("button","Members").should('be.visible')
 //Add account for student4
  cy.contains("Student 4").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('student4','test1234')
})

it('Teacher add assignment', () => {
  cy.ustadClearDbAndLogin('teacher1','test1234')
 // Add Assignment block
  cy.contains("Courses").click()
  cy.contains("004_013").click()
  cy.contains("button","Members").click()  // This is a temporary command to make sure member list is loaded
  cy.contains("button","Course").click()
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
  cy.ustadClearDbAndLogin('student1','test1234')
  cy.contains("Course").click()
  cy.contains("004_013").click()
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
  cy.ustadClearDbAndLogin('student3','test1234')
  cy.contains("Course").click()
  cy.contains("004_013").click()
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
  cy.ustadClearDbAndLogin('student2','test1234')
  cy.contains("Course").click()
  cy.contains("004_013").click()
  cy.contains('Assignment 1').click()
  cy.contains('Keep it up').should('exist')
  cy.contains('9/10 Points').should('exist')
  cy.contains('SUBMIT').should('not.exist') // assertion to make sure multiple submission is not allowed
})
})