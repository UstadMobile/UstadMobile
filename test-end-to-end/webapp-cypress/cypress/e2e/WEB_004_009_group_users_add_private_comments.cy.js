describe('WEB_004_009_group_users_add_private_comments', () => {
it('Start Ustad Test Server ', () => {
 // Start Test Server
  cy.ustadStartTestServer()
})

it('Admin add a course and Members', () => {
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
 // Add a new course
  cy.ustadAddCourse('004_009')
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

it('Teacher add assignment and course comment', () => {
  cy.ustadClearDbAndLogin('teacher1','test1234')
 // Add Assignment block
  cy.contains("Courses").click()
  cy.contains("004_009").click()
  cy.contains("button","Members").click()  // This is a temporary command to make sure member list is loaded
  cy.contains("button","Course").click()
  cy.contains("button","Edit").click()
  cy.contains("Add block").click()
  cy.contains("Assignment").click()
  cy.get('input[id="title"]').type("Assignment 1")
  cy.get('div[data-placeholder="Description"]').type("this is a simple assignment")
  cy.get('#group_submission_on').click()
  cy.get('#cgsName').click()
  cy.get('#add_new_groups',{timeout: 5000}).click()
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
  cy.get('input[id="title"]').clear().type("Assignment 1")
  cy.contains("button","Done").should('be.visible')
  cy.contains("button","Done").click()
  cy.contains("button","Save").should('be.visible')
  cy.contains("button","Save").click()
  cy.contains("button","Members").should('be.visible')
  cy.contains('Assignment 1').click()
  cy.contains('Submissions').click()
  cy.contains('Group 1').click()
  cy.ustadTypeAndSubmitAssignmentComment('#private_comment_textfield','#private_comment_textfield_send_button','comment1')
})

it('Group 1 - Student add private comment', () => {
  cy.ustadClearDbAndLogin('student1','test1234')
  cy.contains("Course").click()
  cy.contains("004_009").click()
  cy.contains('Assignment 1').click()
   cy.get(".VirtualList").scrollTo('bottom')
  cy.ustadTypeAndSubmitAssignmentComment('#private_comment_textfield','#private_comment_textfield_send_button','comment2')
  cy.contains("comment2").ustadScrollUntilVisible()
})

it('Group 2 - Student3 not able to view Group 1 private comments', () => {
  cy.ustadClearDbAndLogin('student3','test1234')
  cy.contains("Course").click()
  cy.contains("004_009").click()
  cy.contains('Assignment 1').click()
  cy.get(".VirtualList").scrollTo('bottom')
  cy.get('#private_comment_textfield').should('exist')
  cy.contains("comment1").should('not.exist')
  cy.contains("comment2").should('not.exist')
})

it('Group 1 - Student2 able to view Group 1 private comment', () => {
  cy.ustadClearDbAndLogin('student2','test1234')
  cy.contains("Course").click()
  cy.contains("004_009").click()
  cy.contains('Assignment 1').click()
  cy.get("#VirtualList").scrollTo('bottom');
  cy.contains("comment1").ustadScrollUntilVisible()
  cy.contains("comment2").should('exist')
})
})