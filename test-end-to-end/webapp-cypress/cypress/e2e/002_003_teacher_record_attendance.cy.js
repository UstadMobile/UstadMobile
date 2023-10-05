describe('Ustad mobile course tests', () => {
  it('002_003_teacher_record_attendance.cy.js', () => {

    cy.login('admin','testpass')

    //Add a course
    cy.contains("Courses").click()
    cy.contains("button","Course").click()
    cy.contains("Add a new course").should('be.visible').click()
    cy.get('input[id="clazz_name"]').type("Class 002_003")
    cy.get('div[data-placeholder="Description"]').type("simple class")
    cy.contains("button","Save").click()

    //Add a teacher
    cy.contains("button","members").click()
    cy.contains("span","Add a teacher").click()
    cy.contains("span","Add a new person").click()
    cy.contains("label", "First names").parent().find("input").clear().type("Teacher")
    cy.contains("label", "Last name").parent().find("input").clear().type("A")
    cy.get('div[id="gender"]').click()
    cy.contains("li","Female").click()
    cy.contains("button","Save").should('be.visible').click()
    cy.wait(2000)
    cy.contains("button","Save").should('be.visible')
    cy.contains("button","Save").click()

     //Add a student

     cy.contains("span","Add a student").click()
     cy.contains("span","Add a new person").click()
     cy.contains("label", "First names").parent().find("input").clear().type("Student")
     cy.contains("label", "Last name").parent().find("input").clear().type("A")
     cy.get('div[id="gender"]').click()
     cy.contains("li","Female").click()
     cy.contains("button","Save").click().should('be.visible')
     cy.wait(2000)
     cy.contains("button","Save").should('be.visible')
     cy.contains("button","Save").click()

     //Add a student

     cy.contains("span","Add a student").click()
     cy.contains("span","Add a new person").click()
     cy.contains("label", "First names").parent().find("input").clear().type("Student")
     cy.contains("label", "Last name").parent().find("input").clear().type("B")
     cy.get('div[id="gender"]').click()
     cy.contains("li","Female").click()
     cy.contains("button","Save").click().should('be.visible')
     cy.wait(2000)
     cy.contains("button","Save").should('be.visible')
     cy.contains("button","Save").click()

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