describe('003_002_add_module_text_blocks_and_perform_indent_hide_delete_actions', () => {
it('Start Ustad Test Server ', () => {
      // Start Test Server
        cy.ustadStartTestServer()
  })
 it('Admin add module and text blocks, then perform indent_hide_delete actions', () => {
      // Admin user login
        cy.ustadClearDbAndLogin('admin','testpass')

      // Add a new course
        cy.ustadAddCourse('005_006')

      // Add module block
         cy.contains('button','Edit').click()
         cy.ustadAddModuleBlock('module 1')


      // Add discussion board
         cy.contains('button','Edit').click()
         cy.ustadAddDiscussionBoard('Discussion 1')
         cy.contains('Edit course').should('be.visible')
         cy.contains("button","Save").click()

      // Add post to the discussion

         cy.contains('005_006').should('be.visible')
         cy.get('[data-testid="ForumIcon"]').click()
         cy.contains('Post').click()
         cy.get('#discussion_post_title').type('Post 1')
         cy.get('.ql-editor.ql-blank').type('Discusssion post')
         cy.get('#actionBarButton').click()
         cy.go('back')
         cy.go('back')

      //Add a teacher
        cy.contains("button","members").click()
        cy.contains("span","Add a teacher").click()
        cy.ustadAddNewPerson('Teacher','A','Female')

      // Add account for teacher
        cy.contains("Teacher A").click()
        cy.contains('View profile').click()
        cy.ustadCreateUserAccount('teacherA','test1234')

      //Add a student
        cy.contains("span","Add a student").click()
        cy.ustadAddNewPerson('Student','A','Male')
        cy.contains("button","members").should('be.visible')


      //Add account for student
        cy.contains("Student A").click()
        cy.contains('View profile').click()
        cy.ustadCreateUserAccount('studentA','test1234')

  })

  it('Teacher able reply to a post ', () => {

    // Teacher Login

     cy.ustadClearDbAndLogin('teacherA','test1234')
     cy.contains("Courses").should('be.visible')
     cy.contains('005_006').click()

    // Add reply to the post board

     cy.get('[data-testid="ForumIcon"]').click()
     cy.contains('Post 1').click()
     cy.get('[data-placeholder="Add a reply"]').type('')
     cy.contains('button','Post').click()
     cy.get('.MuiBox-root.css-z7mtfw').eq(1).should('be.visible')
     })

     it('Student able to reply to a post ', () => {

         // Student Login

          cy.ustadClearDbAndLogin('studentA','test1234')
          cy.contains("Courses").should('be.visible')
          cy.contains('005_003').click()

         // Add reply to the post board

          cy.get('[data-testid="ForumIcon"]').click()
          cy.contains('Post 1').click()
          cy.get('[data-placeholder="Add a reply"]').type('Reply 2 to the post 1')
          cy.contains('button','Post').click()
          cy.get('.MuiBox-root.css-z7mtfw').eq(1).should('be.visible')
          cy.get('.MuiBox-root.css-z7mtfw').eq(2).should('be.visible')
          cy.go('back')
          //cy.contains('2 replies').should('exist')
          //cy.get('.MuiTypography-root.MuiTypography-caption.css-1v2gfp5').shoud('exist')
          })
})