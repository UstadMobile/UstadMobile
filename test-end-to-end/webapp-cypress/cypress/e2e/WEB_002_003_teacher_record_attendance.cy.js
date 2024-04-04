describe('WEB_002_003_teacher_record_attendance', () => {
it('Start Ustad Test Server ', () => {
  // Start Test Server
  cy.ustadStartTestServer()
})

it('Admin record student attendance', () => {
  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
  // Add a new course
  cy.ustadAddCourse('002_003')
  //Add a teacher
  cy.contains("button","Members").click()
  cy.contains("span","Add a teacher").click()
  cy.ustadAddNewPerson('Teacher','002_003','Female')
  // Add account for teacher
  cy.contains("Teacher 002_003").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('teacher23','test1234')
  //Add a student1
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','002_A','Male')
  cy.contains("button","Members").should('be.visible')
  //Add account for student1
  cy.contains("Student 002_A").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('student2A','test1234')
  //Add a student2
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','002_B','Male')
  cy.contains("button","Members").should('be.visible')
  //Add account for student2
  cy.contains("Student 002_B").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('student2B','test1234')
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
  cy.ustadClearDbAndLogin('teacher23','test1234')
  cy.contains('002_003').click()
  cy.contains("button","Attendance").click()
  // Edit recorded attendance
  cy.contains('1 Present, 0 Partial, 1 Absent').should('be.visible')
  cy.contains('.MuiListItemText-secondary','1 Present, 0 Partial, 1 Absent').click()
  cy.contains('Student 002_A').should('be.visible')
  cy.contains('Mark all present').click()
  cy.contains("button","Save").should('be.visible')
  cy.contains("button","Save").click()
  cy.contains('2 Present, 0 Partial, 0 Absent').should('be.visible')
})
})