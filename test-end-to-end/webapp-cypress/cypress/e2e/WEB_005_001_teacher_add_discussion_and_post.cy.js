describe('WEB_005_001_teacher_add_discussion_and_post', () => {
it('Start Ustad Test Server ', () => {
  // Start Test Server
  cy.ustadStartTestServer()
})

it('Admin add teacher and discussion board to the course', () => {
  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
  // Add a new course
  cy.ustadAddCourse('005_001')
  // Add discussion board
  cy.contains('button','Edit').click()
  cy.ustadAddDiscussionBoard('Discussion 1')
  cy.contains('Edit course').should('be.visible')
  cy.contains("button","Save").click()
  //Add a teacher
  cy.contains("button","Members").click()
  cy.contains("span","Add a teacher").click()
  cy.ustadAddNewPerson('Teacher','1','Female')
  // Add account for teacher
  cy.contains("Teacher 1").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('teacher1','test1234')
})

it('Teacher able to add a new discussion board and post to the discussion', () => {
  // Teacher Login
  cy.ustadClearDbAndLogin('teacher1','test1234')
  cy.contains("Courses").should('be.visible')
  cy.contains('005_001').click()
  // Add discussion board
  cy.contains('button','Edit').click()
  cy.ustadAddDiscussionBoard('Discussion 2')
  cy.contains('Edit course').should('be.visible')
  cy.contains("button","Save").click()

  //ensure that save course has finished
  cy.contains("#appbar_title", "005_001")
  cy.contains("Discussion 2").click()
  cy.contains('Post').click()
  cy.get('#discussion_post_title').type('Post Title')
  cy.get('.ql-editor').ustadTypeAndVerify('Discusssion post')
  cy.get('#actionBarButton').click()
})
})