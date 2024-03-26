describe('WEB_003_002_add_module_text_blocks_and_perform_indent_hide_delete_actions', () => {
it('Start Ustad Test Server ', () => {
  // Start Test Server
  cy.ustadStartTestServer()
})

it('Admin add module and text blocks, then perform indent_hide_delete actions', () => {
  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
  // Add a new course
  cy.ustadAddCourse('003_002')
  // Add module block
  cy.contains('button','Edit').click()
  cy.ustadAddModuleBlock('module 1')
  // Add text block
  cy.contains('button','Edit').click()
  cy.ustadAddTextBlock('text 1')
  cy.get('[aria-label="More options"]').eq(1).click(); // Click the second element
  // Hide the text block
  cy.contains("li","Hide").click()
  cy.contains("button","Save").click()
  cy.get('Text 1').should('not.exist');
  // Unhide and Indent the text block
  cy.contains("button","Edit").click()
  cy.get('[aria-label="More options"]').eq(1).should('be.visible').click()
  cy.contains("li","Unhide").click()
  cy.get('[aria-label="More options"]').eq(1).should('be.visible').click()
  cy.contains("li","Indent").click()
  cy.contains("button","Save").click()
  // Add module block
  cy.contains('button','Edit').click()
  cy.ustadAddModuleBlock('module 2')
  // Add Assignment
  cy.contains('button','Edit').click()
  cy.ustadAddAssignmentBlock('Assignment 1')
  cy.get('[aria-label="More options"]').eq(3).should('be.visible').click()
  cy.contains("li","Unindent").click()
  cy.wait(2000) // wait for 2 seconds
  cy.contains("button","Save").click()
  //Delete Assignment block
  cy.contains("button","Edit").click()
  cy.get('[aria-label="More options"]').eq(3).should('be.visible').click()
  cy.contains("li","Delete").click()
  cy.contains("button","Save").click()
  cy.contains("Assignment 1").should('not.exist')
})
})