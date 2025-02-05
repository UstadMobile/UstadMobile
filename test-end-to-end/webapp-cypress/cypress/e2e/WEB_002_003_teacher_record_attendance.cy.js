describe('WEB_002_003_teacher_record_attendance', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Admin record student attendance', () => {

  cy.importUsersViaHttp("Ustad_Teacher_and_Students.csv");
  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
  cy.contains('Test Course Block').click()
  //Add attendance
  cy.contains("button","Attendance").click()
  cy.contains("button","Record attendance").click()
  cy.contains("button","Next").click()
  cy.contains('Student 1').should('be.visible')
  cy.contains('Mark all present').click()
  cy.get('button[aria-label="Absent"]').last().click()
  cy.contains("button","Save").should('be.visible')
  cy.contains("button","Save").click()
  cy.contains('.MuiListItemText-secondary','5 Present, 0 Partial, 1 Absent').should('be.visible')
})

it('Teacher has permission to edit attendance of students ', () => {
  // Teacher Login
  cy.ustadClearDbAndLogin('teach1','testt1')
  cy.contains('Test Course Block').click()
  cy.contains("button","Attendance").click()
  // Edit recorded attendance
  cy.contains('5 Present, 0 Partial, 1 Absent').should('be.visible')
  cy.contains('.MuiListItemText-secondary','5 Present, 0 Partial, 1 Absent').click()
  cy.contains('Student 1').should('be.visible')
  cy.contains('Mark all present').click()
  cy.contains("button","Save").should('be.visible')
  cy.contains("button","Save").click()
  cy.contains('6 Present, 0 Partial, 0 Absent').should('be.visible')
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})