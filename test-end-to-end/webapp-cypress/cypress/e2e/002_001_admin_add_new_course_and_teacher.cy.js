describe('Ustad mobile course tests', () => {
  beforeEach(() => {
    cy.clearIndexedDb('localhost_8087');

    cy.visit('http://localhost:8087/')
  });
  it('002_001', () => {

  //cy.clearLocalStorage('localhost_8087') // clear all local storage
    //cy.visit('http://localhost:8087/')
    cy.get('input[id="username"]').type("admin")
    cy.get('input[id="password"]').type("testpass")
    cy.get('button[id="login_button"]').click()
    cy.contains("Courses").click()
    cy.contains("button","Course").click()
    cy.wait(4000)
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
  })

})