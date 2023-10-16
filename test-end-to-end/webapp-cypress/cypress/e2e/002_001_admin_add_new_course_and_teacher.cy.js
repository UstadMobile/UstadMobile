describe('Ustad mobile course tests', () => {
  it('002_001_admin_add_new_course_and_teacher', () => {

   // Admin user login
    cy.login('admin','testpass')

  // Add a new course
    cy.addCourse('002_001')

    //Add a teacher
    cy.contains("button","members").click()
    cy.contains("span","Add a teacher").click()
    cy.addNewPerson('Teacher','002_001','Female')


  //Add a student
    cy.contains("span","Add a student").click()
    cy.addNewPerson('Student','002_001','Male')
    cy.contains("button","members").should('be.visible')

  // Add account for teacher
    cy.contains("Teacher 002_001").click()
    cy.contains('View profile').click()
    cy.createUserAccount('teacher21','test1234')

  //Add account for student
    cy.contains("Student 002_001").click()
    cy.contains('View profile').click()
    cy.createUserAccount('student21','test1234')

  })

it('002_001_teacher_login', () => {

  // Teacher Login

   cy.login('teacher21','test1234')


   })
})