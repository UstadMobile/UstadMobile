describe('Ustad mobile course tests', () => {
  it('002_003_teacher_record_attendance.cy.js', () => {

     // Admin user login
        cy.login('admin','testpass')

      // Add a new course
        cy.addCourse('002_003')

     //Add a teacher
        cy.contains("button","members").click()
        cy.contains("span","Add a teacher").click()
        cy.addNewPerson('Teacher','002_003','Female')
     // Add account for teacher
        cy.contains("Teacher 002_003").click()
        cy.contains('View profile').click()
        cy.createUserAccount('teacher23','test1234')

     //Add a student1
        cy.contains("span","Add a student").click()
        cy.addNewPerson('Student','002_A','Male')
        cy.contains("button","members").should('be.visible')

     //Add account for student1
        cy.contains("Student 002_A").click()
        cy.contains('View profile').click()
        cy.createUserAccount('student2A','test1234')

     //Add a student2
        cy.contains("span","Add a student").click()
        cy.addNewPerson('Student','002_B','Male')
        cy.contains("button","members").should('be.visible')

     //Add account for student2
        cy.contains("Student 002_B").click()
        cy.contains('View profile').click()
        cy.createUserAccount('student2B','test1234')

     //Add attendance
     cy.contains("button","Attendance").click()
     cy.contains("button","Record attendance").click()
     cy.contains("button","Next").click()
     cy.get('button[aria-label="Present"]').first().click()
     cy.get('button[aria-label="Absent"]').last().click()
     cy.wait(2000)
     cy.contains("button","Save").should('be.visible')
     cy.contains("button","Save").click()
     cy.get('svg[data-testid="CalendarTodayIcon"]').should('be.visible')

  })

  it('002_003_teacher_record_attendance', () => {

    // Teacher Login

     cy.login('teacher23','test1234')
     cy.contains('002_003').click()
     cy.contains("button","Attendance").click()
    /* cy.contains("button","Record attendance").click()
     cy.contains("button","Next").click()
     cy.get('button[aria-label="Absent"]').first().click()
     cy.get('button[aria-label="Present"]').last().click()
     cy.wait(2000)
     cy.contains("button","Save").should('be.visible')
     cy.contains("button","Save").click()
     cy.get('svg[data-testid="CalendarTodayIcon"]').should('be.visible')*/

     // Edit recorded attendance
     cy.wait(2000)
     cy.get('svg[data-testid="CalendarTodayIcon"]',{timeout:1000}).first().click()
     cy.contains('Student 002_A').should('be.visible')
     cy.contains('Mark all present').click()
     cy.wait(2000)
     cy.contains("button","Save").should('be.visible')
     cy.contains("button","Save").click()
     cy.get('svg[data-testid="CalendarTodayIcon"]').should('be.visible')
     })

})