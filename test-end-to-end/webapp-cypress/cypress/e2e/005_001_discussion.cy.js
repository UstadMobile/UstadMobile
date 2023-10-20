describe('003_002_add_module_text_blocks_and_perform_indent_hide_delete_actions', () => {
it('Start Ustad Test Server ', () => {
      // Start Test Server
        cy.ustadStartTestServer()
  })
 it('Admin add module and text blocks, then perform indent_hide_delete actions', () => {
      // Admin user login
        cy.ustadClearDbAndLogin('admin','testpass')

      // Add a new course
        cy.ustadAddCourse('005_001')

      // Add module block
         cy.contains('button','Edit').click()
         cy.ustadAddModuleBlock('module 1')


      // Add discussion board
         cy.contains('button','Edit').click()
         cy.ustadAddDiscussionBoard('Discussion 1')
         cy.contains('Edit course').should('be.visible')
         cy.contains("button","Save").click()

      //Add a teacher
        cy.contains("button","members").click()
        cy.contains("span","Add a teacher").click()
        cy.ustadAddNewPerson('Teacher','A','Female')

      // Add account for teacher
        cy.contains("Teacher A").click()
        cy.contains('View profile').click()
        cy.ustadCreateUserAccount('teacherA','test1234')


  })

  it('Teacher able to add a new discussion board and post to the discussion', () => {

    // Teacher Login

     cy.ustadClearDbAndLogin('teacherA','test1234')
     cy.contains("Courses").should('be.visible')
     cy.contains('005_001').click()

    // Add discussion board
     cy.contains('button','Edit').click()
     cy.ustadAddDiscussionBoard('Discussion 2')
     cy.contains('Edit course').should('be.visible')
     cy.contains("button","Save").click()

     cy.get('[data-testid="ForumIcon"]').eq(1).click()
     cy.contains('Post').click()
     cy.get('#discussion_post_title').type('Post Title')
     cy.get('.ql-editor.ql-blank').type('Discusssion post')
     cy.get('#actionBarButton').click()


     })
})