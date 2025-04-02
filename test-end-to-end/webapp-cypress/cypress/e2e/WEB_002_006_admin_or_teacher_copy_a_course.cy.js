describe('WEB_002_006_admin_or_teacher_copy_a_course.cy.js', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Admin create a course', () => {
 cy.importUsersViaHttp("Ustad_Teacher_and_Students.csv");
  // Admin user create a course
  cy.ustadClearDbAndLogin('admin','testpass')
  cy.contains('Test Course Block').click()
  // Add module block
    cy.contains('button', 'Edit').click();
    cy.contains('Add block').click();
    cy.contains('Module').click();
    cy.get('input[id="title"]').type('Term 1');
    cy.contains('button', 'Done').click();
    cy.contains('Term 1').should('be.visible')
    cy.contains('button', 'Save').click();
  // Add Assignment
   cy.contains('button', 'Edit').click();
   cy.contains('Add block').should('be.visible').click();
   cy.contains('Assignment').click();
   cy.get('input[id="title"]').type('Assignment 1')
   cy.ustadSetDateTime(cy.get("#cbDeadlineDate"), new Date(Date.now() - (365 * 24 * 60 * 60 * 1000))) // last year
   cy.contains('button', 'Done').click()
   cy.contains('Assignment 1').should('be.visible')
  // Set Course start and end dates
   cy.ustadSetDate(cy.get("#clazz_start_time"), new Date(Date.now() - (2 * 365 * 24 * 60 * 60 * 1000))) // 2 years ago
   cy.ustadSetDate(cy.get("#clazz_end_time"), new Date(Date.now() - (365 * 24 * 60 * 60 * 1000))) // last year
  // Add Discussion board
   cy.ustadAddDiscussionBoard('Discussion 1')
   cy.contains('Edit course').should('be.visible')
   cy.contains("button","Save").click()
  // Add post to the discussion
   cy.contains('Test Course Block').should('be.visible')
   cy.contains('.MuiTypography-root','Discussion 1').click()
   cy.contains('Post').click()
   cy.get('#discussion_post_title').type('Post 1')
   cy.get('.ql-editor').ustadTypeAndVerify('Discusssion post')
   cy.get('#actionBarButton').click()
   cy.go('back')
   cy.go('back')
   cy.contains('Assignment 1').should('be.visible')
   cy.contains('Copy').should('be.visible')
   cy.contains('Copy').click()
   cy.contains("#appbar_title", "Copy course").should("be.visible")
   cy.get("input[value='Copy of Test Course Block']").should("be.visible")
   cy.contains('Term 1').should('exist')
   cy.contains('Assignment 1').should('exist')
   cy.get('input[id="clazz_name"]').clear()
   cy.get('input[id="clazz_name"]').type('New Test Course')
   cy.scrollTo('bottom')
   cy.contains('Assignment 1').click()
   cy.ustadSetDateTime(cy.get("#cbDeadlineDate"), new Date(Date.now() + (24 * 60 * 60 * 1000))) // tomorrow
   cy.get('#caSubmissionPolicy').click()
   cy.contains('Can make multiple submissions').click()
   cy.contains("button","Done").should('be.visible')
   cy.contains("button","Done").click()
   cy.ustadSetDate(cy.get("#clazz_start_time"), new Date(Date.now() - (24 * 60 * 60 * 1000))) //yesterday
   cy.get("#clazz_end_time").clear()
   cy.contains('Add block').click();
   cy.contains('Text').click();
   cy.get('input[id="title"]').type('Text 1');
   cy.contains('button', 'Done').click()
   cy.contains('Text 1').should('exist')
  cy.contains('button', 'Save', { timeout: 10000 }).click();
  cy.contains('button', 'Save').should('not.exist');
  cy.wait(5000);
  cy.reload();
  cy.contains('Courses', { timeout: 10000 }).click();
  cy.wait(5000);
  cy.contains('New Test Course', { timeout: 10000 }).should('exist');
   cy.contains('Term 1').should('exist')
   cy.contains('Text 1').should('exist')
   cy.contains('Assignment 1').should('exist')
   cy.contains('Discussion 1').click()
   cy.contains('Post 1').should('not.exist')
   cy.contains('Courses', { timeout: 10000 }).scrollIntoView().should('be.visible').click();
   cy.wait(3000);
   cy.contains('Copy of Test Course Block'', { timeout: 10000 }).scrollIntoView().should('exist');
})

it('Teacher has no permission to add a course ', () => {
  cy.ustadClearDbAndLogin('teach1','testt1')
  cy.contains('All').click()
  cy.contains('Test Course Block').should('exist')
  cy.contains('Test Course Block').click()
  cy.contains('Copy').should('not.exist')
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })

})