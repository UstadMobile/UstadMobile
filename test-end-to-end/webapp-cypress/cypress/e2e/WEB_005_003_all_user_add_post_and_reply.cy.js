describe('WEB_005_003_all_user_add_post_and_reply', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Admin add discussion board and post', () => {
  cy.importUsersViaHttp("Ustad_Teacher_and_Students.csv");
  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  // Add discussion board
  cy.contains('button','Edit').click()
  cy.ustadAddDiscussionBoard('Discussion 1')
  cy.contains('Edit course').should('be.visible')
  cy.contains("button","Save").click()
  // Add post to the discussion
  cy.contains('Test Course Block').should('be.visible')
  cy.contains('.MuiTypography-root','Discussion 1').click()
  cy.contains('Post').click()
  cy.get('#discussion_post_title').type('Topic 1')
  cy.get('.ql-editor').ustadTypeAndVerify('Discusssion post')
  cy.get('#actionBarButton').click()
  cy.go('back')
  cy.go('back')
})

it('Teacher able to add a new post and reply', () => {
  // Teacher Login
  cy.ustadClearDbAndLogin('teach1','testt1')
  cy.contains("Courses").should('be.visible')
  cy.contains('Test Course Block').click()
  // Add reply to the post board
  cy.contains('.MuiTypography-root','Discussion 1').click()
  cy.contains('Topic 1').click()
  cy.get('[data-placeholder="Add a reply"]').type('Reply from teacher',{delay : 20})
  cy.contains('button','Post').click()
  cy.contains('Reply from teacher').should('be.visible')
  cy.go('back')
  cy.go('back')
  // Teacher add new post -2
  cy.contains('Discussion 1').click()
  cy.contains('.MuiButtonBase-root','Post').click()
  cy.get('#discussion_post_title').type('Topic 2')
  cy.get('.ql-editor').ustadTypeAndVerify('Discusssion post')
  cy.get('#actionBarButton').click()
})

it('Student able to add a post and reply', () => {
  // Student Login
  cy.ustadClearDbAndLogin('stud1','tests1')
  cy.contains("Courses").should('be.visible')
  cy.contains('Test Course Block').click()
  // Add reply to the post board
  cy.contains('Discussion 1').click()
  cy.contains('Topic 1').click()
  cy.get('[data-placeholder="Add a reply"]').type('Reply from Student',{delay : 20})
  cy.contains('button','Post').click()
  cy.contains('Reply from teacher').should('be.visible')
  cy.contains('Reply from Student').should('be.visible')
  cy.go('back')
  cy.go('back')
  // Student add new post -3
  cy.contains('Discussion 1').click()
  cy.contains('.MuiButtonBase-root','Post').click()
  cy.get('#discussion_post_title').type('Topic 3')
  cy.get('.ql-editor').ustadTypeAndVerify('Discusssion post')
  cy.get('#actionBarButton').click()
  cy.contains('Topic 3').should('be.visible')
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})