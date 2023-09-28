  describe('Ustad mobile course tests', () => {
  beforeEach(() => {
    cy.clearIndexedDb('localhost_8087');

    cy.visit('http://localhost:8087/')
  });

    it('004_001_teacher_add_assignment_and_grade', () => {
   // cy.visit('http://localhost:8087/')
    cy.get('input[id="username"]').type("admin")
    cy.get('input[id="password"]').type("testpass")
    cy.get('button[id="login_button"]').click()
    cy.wait(4000)
    cy.contains('Courses').should('be.visible')

    // Add new course
     cy.contains("Courses").click()
     cy.contains("button","Course").click()
     cy.wait(4000)
     cy.contains("Add a new course").should('be.visible')
     cy.contains("Add a new course").click()
     cy.get('input[id="clazz_name"]').type("New class")
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

   // Add Assignment block
     cy.contains("Course").click()
     cy.contains("New class").click()
     cy.contains("button","Edit").click()
     cy.contains("Add block").click()
     cy.contains("Assignments").click()
     cy.get('input[id="title"]').type("Assignment 1")
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
                   input.val('2023-07-01T13:00')
                 })
                 .click({ multiple: true }).should('be.visible')
    cy.wait(2000)
    cy.contains("button","Done").should('be.visible')
    cy.contains("button","Done").click()
    cy.get('svg[data-testid="MoreVertIcon"]').last().click().should('be.visible')
    cy.contains("button","Save").should('be.visible')
    cy.contains("button","Save").click()
    cy.contains("span","Assignment 1").click()
    cy.get('input[id="course_comment_textfield"]').type("comment 1")
    cy.get('svg[data-testid="SendIcon"]').click()
    })

  })