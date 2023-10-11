describe('Ustad mobile course tests', () => {
 it('003_002_add_module_text_blocks_and_perform_indent_hide_delete_actions', () => {
    cy.login('admin','testpass')
    // Add new course
    cy.contains("Courses").click()
    cy.contains("button","Course").click()
    cy.contains("Add a new course").click()
    cy.get('input[id="clazz_name"]').type("Class 003_002")
    cy.get('div[data-placeholder="Description"]').type("simple class")
    // Add module block
    cy.contains("Add block").click()
    cy.contains("Module").click()
    cy.get('input[id="title"]').type("module 1")
    cy.contains("button","Done").click()
    cy.contains("button","Save").click()
    // Add text block
    cy.contains("button","Edit").click()
    cy.contains("Add block").click()
    cy.contains("Text").click()
    cy.get('input[id="title"]').type("Text 1")
    cy.get('div[data-placeholder="Description"]').type("a simple block test")
    cy.contains("button","Done").click()

    cy.wait(2000) //if we comment this wait command then the hide button not getting clicked
    cy.get('svg[data-testid="MoreVertIcon"]').last().should('be.visible').click()
    cy.contains("li","Hide").click()
    cy.wait(2000) // wait for 2 seconds
    cy.contains("button","Save").click()
    cy.get('Text 1').should('not.be.visible');

   // text block unhide and indent
    cy.contains("button","Edit").click()
    cy.wait(2000) // wait for 2 seconds
    cy.get('svg[data-testid="MoreVertIcon"]').last().should('be.visible').click()
    cy.contains("li","Unhide").click()
    //cy.wait(2000) // wait for 2 seconds
    cy.get('svg[data-testid="MoreVertIcon"]').last().should('be.visible').click()
    cy.contains("li","Indent").click()
    cy.wait(2000) // wait for 2 seconds
    cy.contains("button","Save").click()

    // Add module block
    cy.contains("button","Edit").click()
    cy.contains("Add block").click()
    cy.contains("Module").click()
    cy.get('input[id="title"]').type("module 2")
    cy.contains("button","Done").click()
    //cy.wait(2000) // wait for 2 seconds

    // Add Assignment block
    cy.contains("Add block").should('be.visible').click()
    cy.contains("Assignments").click()
    cy.get('input[id="title"]').type("Assignment 1")
    cy.get('div[data-placeholder="Description"]').type("this is a simple assignment")
    cy.contains("button","Done").click()
    //cy.wait(2000) // wait for 2 seconds
    cy.get('svg[data-testid="MoreVertIcon"]').last().should('be.visible').click()
    cy.contains("li","Unindent").click()
    cy.wait(2000) // wait for 2 seconds
    cy.contains("button","Save").click()

    //Delete Assignment block
     cy.contains("button","Edit").click()
     cy.wait(2000) // wait for 2 seconds
     cy.get('svg[data-testid="MoreVertIcon"]').last().should('be.visible').click()
     cy.contains("li","Delete").click()
     cy.wait(2000) // wait for 2 seconds
     cy.contains("button","Save").click()
     cy.contains("Assignment 1").should('not.be.visible')
  })

/* it('003_003_add_existing_content_in_library_as_block', () => {

 })*/
 /*
  it('003_005_add_new_content_block_inside_course', () => {

  })*/
 /*
   it('003_006_course_view_and_modules_can_expand_collapse', () => {

   })*/
})