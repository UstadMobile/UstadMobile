describe('WEB_005_002_teacher_can_edit_discussion_board', () => {
it('Start Ustad Test Server ', () => {
  // Start Test Server
  cy.ustadStartTestServer()
})

it('Admin add discussion board and a post', () => {
  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
  // Add a new course
  cy.ustadAddCourse('005_002')
  // Add discussion board
  cy.contains('button','Edit').click()
  cy.ustadAddDiscussionBoard('Discussion 1')
  cy.contains('Edit course').should('be.visible')
  cy.contains("button","Save").click()
  // Add post to the discussion
  cy.contains('005_002').should('be.visible')
  cy.contains('.MuiTypography-root','Discussion 1').click()
  cy.contains('Post').click()
  cy.get('#discussion_post_title').type('Post Title')
  cy.get('.ql-editor').ustadTypeAndVerify('Discusssion post')
  cy.get('#actionBarButton').click()
  cy.go('back')
  cy.go('back')
  //Add a teacher
  cy.contains("button","Members").click()
  cy.contains("span","Add a teacher").click()
  cy.ustadAddNewPerson('Teacher','1','Female')
  // Add account for teacher
  cy.contains("Teacher 1").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('teacher1','test1234')
})

it('Teacher able to edit discussion board ', () => {
  // Teacher Login
  cy.ustadClearDbAndLogin('teacher1','test1234')
  cy.contains("Courses").should('be.visible')
  cy.contains('005_002').click()
  // Add discussion board
  cy.contains('button','Edit').click()
  cy.contains('Discussion 1').click()
  cy.get('div[data-placeholder="Description"]').clear().type("teacher edit discussion description")
  cy.get('div[data-placeholder="Description"]').ustadTypeAndVerify("teacher edit discussion description")
  cy.contains("button","Done").click()
})
})