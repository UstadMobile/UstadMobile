describe('002_003_teacher_record_attendance', () => {
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
  cy.contains("button","members").click()
  cy.contains("span","Add a teacher").click()
  cy.ustadAddNewPerson('Teacher','1','Female')
  // Add account for teacher
  cy.contains("Teacher 1").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('teacher1','test1234')
  //Add a student1
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','1','Male')
  cy.contains("button","members").should('be.visible')
  //Add account for student1
  cy.contains("Student 1").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('student1','test1234')
  //Add a student2
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','2','Male')
  cy.contains("button","members").should('be.visible')
  //Add account for student2
  cy.contains("Student 2").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('student2','test1234')
  //Add attendance
  cy.contains("button","Attendance").click()
  cy.contains("button","Record attendance").click()
  cy.contains("button","Next").click()
  cy.get('button[aria-label="Present"]').first().click()
  cy.get('button[aria-label="Absent"]').last().click()
  cy.contains("button","Save").should('be.visible')
  cy.contains("button","Save").click()
  cy.contains('1 Present, 0 Partial, 1 Absent').should('be.visible')
  cy.get('svg[data-testid="CalendarTodayIcon"]').should('be.visible')
})

it('Teacher has permission to edit attendance of students ', () => {
  // Teacher Login
  cy.ustadClearDbAndLogin('teacher1','test1234')
  cy.contains('002_003').click()
  cy.contains("button","Attendance").click()
  // Edit recorded attendance
  cy.contains('1 Present, 0 Partial, 1 Absent').should('be.visible')
  cy.get('svg[data-testid="CalendarTodayIcon"]',{timeout:2000}).first().click()
  cy.contains('Student 1').should('be.visible')
  cy.contains('Mark all present').click()
  cy.contains("button","Save").should('be.visible')
  cy.contains("button","Save").click()
  cy.contains('2 Present, 0 Partial, 0 Absent').should('be.visible')
})
})