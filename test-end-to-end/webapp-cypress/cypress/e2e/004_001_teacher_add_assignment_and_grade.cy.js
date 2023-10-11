  describe('Ustad mobile course tests', () => {
    it('004_001_teacher_add_assignment_and_grade', () => {
    cy.login('admin','testpass')
    cy.contains('Courses').should('be.visible')

    // Add new course
     cy.contains("Courses").click()
     cy.contains("button","Course").click()
     cy.wait(4000)
     cy.contains("Add a new course").should('be.visible')
     cy.contains("Add a new course").click()
     cy.get('input[id="clazz_name"]').type("class 004_001")
     cy.get('div[data-placeholder="Description"]').type("simple class")
    // Add module block
     cy.contains("Add block").click()
     cy.contains("Module").click()
     cy.get('input[id="title"]').type("module 1")
     cy.contains("button","Done").click()
     cy.contains("button","Save").click()

      //Add a student
     cy.contains("button","members").click()
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
/*
     //Add account for student
            cy.contains("Student A").click()
            cy.contains('View profile').click()
            cy.contains('Create account').click()
            cy.get('#username',{timeout: 1000}).should('not.be.disabled').type('s1')
            cy.get('#newpassword').type('1234')
            cy.wait(2000)
            cy.contains("button","Save").click().should('be.visible')
            cy.contains('Change Password').should('be.visible')
            */

   // Add Assignment block
     cy.contains("Course").click()
     cy.contains("004_001").click()
     cy.contains("button","Edit").click()
     cy.contains("Add block").click()
     cy.contains("Assignments").click()
     cy.get('input[id="title"]').type("Assignment 2")
     cy.get('div[data-placeholder="Description"]').type("this is a simple assignment")
     cy.get('input[id="hide_until_date"]').click()
   // cy.get('input[type="datetime-local"]').type("05-31-2019T10:37")
  // cy.contains("span","Don\'t show before (Optional)").click()
     cy.wait(2000)
     cy.get('#hide_until_date')
             .click({ multiple: true })
             .then(input => {
               input[0].dispatchEvent(new Event('input', { bubbles: true }))
               input.val('2023-06-01T13:00')
             })
             .should('be.visible')
     cy.get('#hide_until_date')
             .click({ multiple: true })
             .then(input => {
             input[0].dispatchEvent(new Event('input', { bubbles: true }))
             input.val('2023-06-01T13:00')
           })
           .click({ multiple: true })
    cy.wait(2000)
    cy.contains("div","Graded").click()
    cy.contains("li","submitted").click()
    cy.wait(2000)
    cy.get('#cbDeadlineDate')
                 .click({ multiple: true })
                 .then(input => {
                   input[0].dispatchEvent(new Event('input', { bubbles: true }))
                   input.val('2025-07-01T13:00')
                 })
                 .click({ multiple: true }).should('be.visible')
    cy.wait(2000)
    cy.contains("button","Done").should('be.visible')
    cy.contains("button","Done").click()

  //cy.contains("span", "Assignment 2").find('svg[data-testid="MoreVertIcon"]').click()

   //  cy.contains("Assignment 2", {timeout: 4000}).click()
    //cy.get('svg[data-testid="MoreVertIcon"]', {timeout: 1000}).last().click().should('be.visible')
    //cy.contains("li","Indent").click()
    //cy.wait(2000) // wait for 2 seconds
     cy.contains("button","Save").should('be.visible')
    cy.contains("button","Save").click()

    })
   /* it('004_001_student', () => {
        cy.login('sa','1234')
        })*/

  })