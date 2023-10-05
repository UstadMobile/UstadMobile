describe('Ustad mobile course tests', () => {
  it('002_001_admin_add_new_course_and_teacher', () => {
    cy.login('admin','testpass')
    //create a new course
    cy.contains("Courses").click()
    cy.contains("button","Course").click()
    //cy.wait(4000)
    cy.contains("Add a new course").click()
    cy.get('input[id="clazz_name"]').type("Class 002_001")
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
    cy.contains("button","Save").click().should('be.visible')
    cy.wait(2000)
    //cy.get('button[id="actionBarButton"]',{timeout:6000}).should('be.visible')
    cy.contains("button","Save").click().should('be.visible')
   /* cy.contains("Teacher A").click()
    cy.contains('View profile').click()
    cy.contains('Create account').click()
    cy.contains("label", "Username",{timeout:2000}).parent().find("input").clear().type("ta")
    cy.contains("Teacher A").click()
    cy.contains("label", "New Password",{timeout:2000}).parent().find("input").clear().type("test")
    cy.wait(2000)
    cy.contains("button","Save").click().should('be.visible')*/

     //Add a student

        cy.contains("span","Add a student").click()
        cy.contains("span","Add a new person").click()
        cy.contains("label", "First names").parent().find("input").clear().type("Student")
        cy.contains("label", "Last name").parent().find("input").clear().type("A")
        cy.get('div[id="gender"]').click()
        cy.contains("li","Female").click()
        cy.contains("button","Save").click().should('be.visible')
        cy.wait(2000)
       // cy.contains("button","Save").should('be.visible')
        cy.contains("button","Save").click()
        cy.contains("button","members").should('be.visible')
  })

})