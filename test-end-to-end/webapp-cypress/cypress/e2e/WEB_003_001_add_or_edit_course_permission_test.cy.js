describe('WEB_003_001_add_or_edit_course_permission_test', () => {
it('Start Ustad Test Server ', () => {
  // Start Test Server
  cy.ustadStartTestServer()
})

it('Admin has course edit permission', () => {
  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
  // Add a new course
  cy.ustadAddCourse('003_001')
  // Admin test the course edit permission
  cy.contains('button','Edit').click()
  cy.get('div[data-placeholder="Description"][contenteditable="true"]',{timeout: 5000}).clear()
  cy.get('div[data-placeholder="Description"][contenteditable="true"]').type("Admin has edit permission")
  cy.contains("button","Save").click()
  cy.contains("Admin has edit permission").should('be.visible')
  //Add a teacher
  cy.contains("button","Members").click()
  cy.contains("span","Add a teacher").click()
  cy.ustadAddNewPerson('Teacher','003','Female')
  // Add account for teacher
  cy.contains("Teacher 003").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('teacher3','test1234')
  //Add a student1
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','003','Male')
  cy.contains("button","Members").should('be.visible')
  //Add account for student1
  cy.contains("Student 003").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('student3','test1234')
})

it('Teacher have the permission to edit the course ', () => {
  // Teacher Login
  cy.ustadClearDbAndLogin('teacher3','test1234')
  cy.contains('003_001').click()
  // Teacher test the course edit permission
  cy.contains('button','Edit').click()
  cy.get('div[data-placeholder="Description"][contenteditable="true"]').clear().type("Teacher has edit permission")
  cy.contains("button","Save").click()
  cy.contains("Teacher has edit permission").should('be.visible')
})

it('Student does not have the course edit permission ', () => {
  // Student Login
  cy.ustadClearDbAndLogin('student3','test1234')
  cy.contains('003_001').click()
  // Student doesn't have the course edit permission
  cy.contains('button','Edit').should('not.exist')
})
})