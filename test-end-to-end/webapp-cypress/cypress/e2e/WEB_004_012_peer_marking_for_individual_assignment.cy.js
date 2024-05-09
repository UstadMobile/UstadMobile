describe('WEB_004_012_peer_marking_for_individual_assignment', () => {
it('Start Ustad Test Server ', () => {
 // Start Test Server
  cy.ustadStartTestServer()
})

it('Admin add a course and assignment block', () => {
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
 // Add a new course
  cy.ustadAddCourse('004_012')
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
 //Add account for student2
  cy.contains("Student 2").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('student2','test1234')
})

it('Teacher add assignment', () => {
  cy.ustadClearDbAndLogin('teacher1','test1234')
 // Add Assignment block
  cy.contains("Course").click()
  cy.contains("004_012").click()
  cy.contains("button","Members").click()  // This is a temporary command to make sure member list is loaded
  cy.contains("button","Course").click()
  cy.contains("button","Edit").click()
  cy.contains("Add block").click()
  cy.contains("Assignment").click()
  cy.get('input[id="title"]').clear().type("Assignment 1")
  cy.get('div[data-placeholder="Description"]').type("this is a simple assignment")
  cy.contains("div","Graded").click()
  cy.contains("li","Submitted").click()
  cy.ustadSetDateTime(cy.get("#cbDeadlineDate"),  new Date(Date.now() + (1000*60*1000)))
  cy.get('#caMarkingType').click()
  cy.contains("li","Peers").click()
  cy.get('#caPeerReviewerCount').should('exist')
  cy.get('#caPeerReviewerCount').type('1')
  cy.wait(1000) // wait to load the student list - tests getting failed without this wait.
  cy.get('#buttonAssignReviewers',{timeout:2000}).click()
  cy.contains('Student 1').should('be.visible',{timeout:2000})
  cy.contains('Unassigned').eq(0).click()
  cy.get('li[role="option"]').eq(1).should('be.visible')
  cy.get('li[role="option"]').eq(1).click()
  cy.contains('Unassigned').eq(0).click()
  cy.get('li[role="option"]').eq(1).should('be.visible')
  cy.get('li[role="option"]').eq(1).click()

  //Click done on role assignment
  cy.contains("button","Done").should('be.visible')
  cy.contains("button","Done",{timeout:1000}).click()

  //Click done on assignment edit
  cy.url().should('include', "CourseAssignmentEdit") //Ensure that navigation to assignment edit done
  cy.contains("button","Done",{timeout:3000}).click()

  cy.contains("button","Save",{timeout:2000}).should('be.visible')
  cy.contains("button","Save",{timeout:1000}).click()

  cy.contains("button","Members").should('be.visible')
  cy.contains("button","Edit").click()
  cy.contains("Assignment 1").click()
  cy.contains("button","Done").should('be.visible')
  cy.contains("button","Done").click()
  cy.contains("button","Save").should('be.visible')
  cy.contains("button","Save").click()
  cy.contains("button","Members").should('be.visible')
})

it('Student1 submit assignment', () => {

  cy.ustadClearDbAndLogin('student1','test1234')
  cy.contains("Course").click()
  cy.contains("004_012").click()
  cy.contains('Assignment 1').click()
  cy.get('#assignment_text').get('div[contenteditable="true"]',{timeout:6000}).should('be.visible')
  cy.get('#assignment_text').click()
  cy.get('.ql-editor').ustadTypeAndVerify('Text 1')
  cy.contains('SUBMIT',{timeout:5000}).click()
  cy.contains("Submission 1").should("be.visible")
  cy.contains("Text 1").should("be.visible")

  cy.go('back')
  cy.contains('Assignment 1',{timeout:1000}).click()
  cy.contains("Not submitted").should('not.exist')
})

it('Student2 add assignment mark and course comment', () => {
  cy.ustadClearDbAndLogin('student2','test1234')
  cy.contains("Course").click()
  cy.contains("004_012").click()
  cy.contains("button","Course").click()
  cy.contains("Assignment 1").click()
  cy.contains('Peers to review').click()
  cy.ustadReloadUntilVisible("Student 1")
  cy.contains("Student 1").click()
  cy.get('#marker_comment').type("Keep it up")
  cy.get('#marker_mark').type('9')
  cy.get('#submit_mark_button').click()
  cy.contains('Keep it up').should('exist')
  cy.contains('9/10 Points').should('exist')
})

it('Student1 view his grade', () => {
  cy.ustadClearDbAndLogin('student1','test1234')
  cy.contains("Course").click()
  cy.contains("004_012").click()
  cy.contains('Assignment 1').click()
  cy.contains('Keep it up').should('exist')
  cy.contains('9/10 Points').should('exist')
  cy.contains('SUBMIT').should('not.exist') // assertion to make sure multiple submission is not allowed
  })
})