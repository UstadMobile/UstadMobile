describe('WEB_003_001_add_or_edit_course_permission_test', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Admin has course edit permission', () => {

  cy.importUsersViaHttp("Ustad_Teacher_and_Students.csv");
  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
  cy.contains('Test Course Block').click()
  cy.contains('button','Edit').click()
  cy.get('div[data-placeholder="Description"][contenteditable="true"]',{timeout: 5000}).clear()
  cy.get('div[data-placeholder="Description"][contenteditable="true"]').type("Admin has edit permission")
  cy.contains("button","Save").click()
  cy.contains("Admin has edit permission").should('be.visible')
})

it('Teacher have the permission to edit the course ', () => {
  // Teacher Login
  cy.ustadClearDbAndLogin('teach1','testt1')
  cy.contains('Test Course Block').click()
  // Teacher test the course edit permission
  cy.contains('button','Edit').click()
  cy.get('div[data-placeholder="Description"][contenteditable="true"]').clear().type("Teacher has edit permission")
  cy.contains("button","Save").click()
  cy.contains("Teacher has edit permission").should('be.visible')
})

it('Student does not have the course edit permission ', () => {
  // Student Login
  cy.ustadClearDbAndLogin('stud1','tests1')
  cy.contains('Test Course Block').click()
  // Student doesn't have the course edit permission
  cy.contains('button','Edit').should('not.exist')
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})