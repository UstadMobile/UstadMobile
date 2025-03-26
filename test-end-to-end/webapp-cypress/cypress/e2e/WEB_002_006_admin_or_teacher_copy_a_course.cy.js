describe('WEB_002_006_admin_or_teacher_copy_a_course.cy.js', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Admin create a course', () => {

  cy.importUsersViaHttp("Ustad_Teacher_and_Students.csv");
  // Admin user login
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
   cy.ustadSetDateTime(cy.get("#hide_until_date"), new Date("2023-12-01T08:30"))
   cy.ustadSetDateTime(cy.get("#cbDeadlineDate"), new Date("2024-01-01T08:30"))
   cy.contains('button', 'Done').click()
   cy.contains('Assignment 1').should('be.visible')
   cy.ustadBirthDate(cy.get("#clazz_start_time"), new Date("2023-11-01"))
   cy.ustadBirthDate(cy.get("#clazz_end_time"), new Date("2024-11-01"))
   cy.contains('button', 'Save').click()
   cy.contains('Assignment 1').should('be.visible')
  cy.contains('Members').should('be.visible')
})

it('Teacher has permission to copy a course ', () => {
  // Teacher Login
  cy.ustadClearDbAndLogin('teach1','testt1')
  cy.contains('All').click()
  cy.contains('Test Course Block').should('exist')
  cy.contains('Test Course Block').click()
  cy.contains('Copy').click()
  cy.contains("#appbar_title", "Copy course").should("be.visible")
  cy.get("input[value='Copy of Test Course Block']").should("be.visible")
  cy.contains('Term 1').should('exist')
  cy.contains('Assignment 1').should('exist')
  cy.contains('Assignment 1').click()
  cy.ustadSetDateTime(cy.get("#hide_until_date"), new Date("2025-01-01T08:30"))
  cy.ustadSetDateTime(cy.get("#cbDeadlineDate"), new Date("2025-10-01T08:30"))
  cy.get('#caSubmissionPolicy').click()
  cy.contains('Can make multiple submissions').click()
  cy.get("#caClassCommentEnabled").click()
  cy.contains("button","Done").should('be.visible')
  cy.contains("button","Done").click()
  cy.get('input[id="clazz_name"]').clear().type('New Test Course')
  cy.ustadBirthDate(cy.get("#clazz_start_time"), new Date("2025-01-01"))
  cy.ustadBirthDate(cy.get("#clazz_end_time"), new Date("2026-01-01"))
  cy.contains('button', 'Save').click()
  cy.contains('Courses').click()
  cy.contains('New Test Course').should('exist')
})

it('student user cannot copy a course', () => {
 // student user login
  cy.ustadClearDbAndLogin('stud1','tests1')
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