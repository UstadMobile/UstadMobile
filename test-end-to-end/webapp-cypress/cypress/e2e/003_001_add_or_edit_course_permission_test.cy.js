describe('Ustad mobile course tests', () => {

  beforeEach(() => {
    cy.clearIndexedDb('localhost_8087');

    cy.visit('http://localhost:8087/')
  });


  it('003_001_add_or_edit_course_permission_test', () => {
  //  cy.visit('http://localhost:8087/')
    //cy.get('input[id="sitelink_textfield"]').type("http://localhost:8087/")
  //  cy.get('button[id="next_button"]').click()
    cy.get('input[id="username"]').type("admin")
    cy.get('input[id="password"]').type("testpass")
    cy.get('button[id="login_button"]').click()
    cy.contains("Courses").click()
    cy.contains("button","Course").click()
    cy.contains("Add a new course").click()
    cy.get('input[id="clazz_name"]').type("New class")
    cy.get('div[data-placeholder="Description"]').type("simple class")
    cy.contains("button","Save").click()
  })

})