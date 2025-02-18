describe('WEB_005_001_teacher_add_discussion_and_post', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Teacher able to add a new discussion board and post to the discussion', () => {
 // Teacher Login
  cy.importUsersViaHttp("Ustad_Teacher_and_Students.csv");
  cy.ustadClearDbAndLogin('teach1','testt1')
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
 // Add discussion board
  cy.contains('button','Edit').click()
  cy.ustadAddDiscussionBoard('Discussion 1')
  cy.contains('Edit course').should('be.visible')
  cy.contains("button","Save").click()

  //ensure that save course has finished
  cy.contains("#appbar_title", "Test Course Block")
  cy.contains("Discussion 1").click()
  cy.contains('Post').click()
  cy.get('#discussion_post_title').type('Post Title')
  cy.get('.ql-editor').ustadTypeAndVerify('Discusssion post')
  cy.get('#actionBarButton').click()

 //Verify the teacher able to edit discussion board
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
// Edit discussion board
  cy.contains('button','Edit').click()
  cy.contains('Discussion 1').click()
  cy.get('.ql-editor').clear().ustadTypeAndVerify('Discusssion test')
  cy.get('#actionBarButton').click()
  cy.contains("button","Save").click()
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})