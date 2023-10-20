  describe('004_001_teacher_add_assignment_and_grade', () => {
    it('Start Ustad Test Server ', () => {
      // Start Test Server
        cy.ustadStartTestServer()
    })
      it('Admin user create a assignment block to the course', () => {

     // Admin user login
       cy.ustadClearDbAndLogin('admin','testpass')
       cy.contains("Courses").should('be.visible')  // Assertion to check the user logged in successfully

     // Add a new course
       cy.ustadAddCourse('004_001')

     // Add module block
       cy.contains('button','Edit').click()
       cy.ustadAddModuleBlock('module 1')

     //Add a student
       cy.contains("button","members").click()
       cy.contains("span","Add a student").click()
       cy.ustadAddNewPerson('Student','A','Male')
       cy.contains("button","members").should('be.visible')

     //Add account for student
       cy.contains("Student A").click()
       cy.contains('View profile').click()
       cy.ustadCreateUserAccount('studentA','test1234')

     // Add Assignment block
       cy.contains("button","Course").click()
       cy.contains('button','Edit').click()
       cy.ustadAddAssignmentBlock('Assignment 1','2023-06-01T13:00')

  //cy.contains("span", "Assignment 2").find('svg[data-testid="MoreVertIcon"]').click()

   //  cy.contains("Assignment 2", {timeout: 4000}).click()
    //cy.get('svg[data-testid="MoreVertIcon"]', {timeout: 1000}).last().click().should('be.visible')
    //cy.contains("li","Indent").click()
    //cy.wait(2000) // wait for 2 seconds
     cy.contains("button","Save").should('be.visible')
    cy.contains("button","Save").click()

    })
   /* it('004_001_student', () => {
        cy.login('sa','1234')
        })*/

  })