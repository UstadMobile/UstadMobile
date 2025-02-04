describe('WEB_002_003_teacher_record_attendance', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Admin record student attendance', () => {
  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
  // Add a new course
  cy.ustadAddCourse('Test Course')
  //Add a teacher
  cy.contains("button","Members").click()
  cy.contains("span","Add a teacher").click()
  cy.ustadAddNewPerson('Teacher','1','Female')
  // Add account for teacher
  cy.contains("Teacher 1").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('teach1','testt1')
  //Add a student1
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','1','Male')
  cy.contains("button","Members").should('be.visible')
  //Add account for student1
  cy.contains("Student 1").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('stud1','tests1')
  //Add a student2
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','2','Male')
  cy.contains("button","Members").should('be.visible')
  //Add account for student2
  cy.contains("Student 2").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('stud2','tests2')
  //Add attendance
  cy.contains("button","Attendance").click()
  cy.contains("button","Record attendance").click()
  cy.contains("button","Next").click()
  cy.get('button[aria-label="Present"]').first().click()
  cy.get('button[aria-label="Absent"]').last().click()
  cy.contains("button","Save").should('be.visible')
  cy.contains("button","Save").click()
  cy.contains('.MuiListItemText-secondary','1 Present, 0 Partial, 1 Absent').should('be.visible')
})

it('Teacher has permission to edit attendance of students ', () => {
  // Teacher Login
  cy.ustadClearDbAndLogin('teach1','testt1')
  cy.contains('Test Course').click()
  cy.contains("button","Attendance").click()
  // Edit recorded attendance
  cy.contains('1 Present, 0 Partial, 1 Absent').should('be.visible')
  cy.contains('.MuiListItemText-secondary','1 Present, 0 Partial, 1 Absent').click()
  cy.contains('Student 1').should('be.visible')
  cy.contains('Mark all present').click()
  cy.contains("button","Save").should('be.visible')
  cy.contains("button","Save").click()
  cy.contains('2 Present, 0 Partial, 0 Absent').should('be.visible')
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})