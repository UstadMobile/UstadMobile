describe('Ustad mobile course tests', () => {
  it('002_003_teacher_record_attendance.cy.js', () => {

     // Admin user login
        cy.login('admin','testpass')

      // Add a new course
        cy.addCourse('002_003')

     //Add a teacher
        cy.contains("button","members").click()
        cy.contains("span","Add a teacher").click()
        cy.addNewPerson('Teacher','A','Female')
     // Add account for teacher
        cy.contains("Teacher A").click()
        cy.contains('View profile').click()
        cy.createUserAccount('teacherA','test1234')

     //Add a student1
        cy.contains("span","Add a student").click()
        cy.addNewPerson('Student','A','Male')
        cy.contains("button","members").should('be.visible')

     //Add account for student1
        cy.contains("Student A").click()
        cy.contains('View profile').click()
        cy.createUserAccount('studentA','test1234')

     //Add a student2
        cy.contains("span","Add a student").click()
        cy.addNewPerson('Student','B','Male')
        cy.contains("button","members").should('be.visible')

     //Add account for student2
        cy.contains("Student B").click()
        cy.contains('View profile').click()
        cy.createUserAccount('studentB','test1234')

     //Add attendance
     cy.contains("button","Attendance").click()
     cy.contains("button","Record attendance").click()
     cy.contains("button","Next").click()
     cy.get('button[aria-label="Present"]').first().click()
     cy.get('button[aria-label="Absent"]').last().click()
     cy.wait(2000)
     cy.contains("button","Save").should('be.visible')
     cy.contains("button","Save").click()

  })

})