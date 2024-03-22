describe('WEB_003_006_user_able_to_expand _and_collapse_modules', () => {
it('Start Ustad Test Server ', () => {
  // Start Test Server
  cy.ustadStartTestServer()
})

it('User able to expand and collapse the module blocks', () => {
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
  cy.get('[aria-label="More options"]').eq(1).click()
  cy.contains("li","Indent").click()
  cy.contains("button","Save").click()
  // Add module block
  cy.contains("button","Edit").click()
  cy.ustadAddModuleBlock('module 2')
  // Add text block
  cy.contains("button","Edit").click()
  cy.ustadAddTextBlock('text 2')
  cy.get('[aria-label="More options"]').eq(1).click()
  cy.contains("li","Indent").click()
  cy.contains("button","Save").click()

  //Testing Module Collapse icon working or not
  cy.contains(".MuiListItem-root", "module 1").find('button[aria-label="Collapse"]').click()
  cy.contains('text 1').should('not.exist');

  //Expand module again
  cy.contains(".MuiListItem-root", "module 1").find('button[aria-label="Expand"]').click()
  cy.contains('text 1').should('exist');
})
})