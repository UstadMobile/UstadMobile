describe('Ustad mobile course tests', () => {

  it('003_001_add_or_edit_course_permission_test', () => {
    cy.login('admin','testpass')
    cy.contains("Courses").click()
    cy.contains("button","Course").click()
    cy.contains("Add a new course").click()
    cy.get('input[id="clazz_name"]').type("Class 003_001")
    cy.get('div[data-placeholder="Description"]').type("simple class")
    cy.contains("button","Save").click()
  })

})