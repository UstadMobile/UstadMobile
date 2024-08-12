describe('WEB_003_007_user_able_to_open_text_block', () => {
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
  cy.contains('button','Edit').should('exist')
  cy.contains('text 1').click()
  cy.get('#courseblock_title').should('be.visible').invoke('text').should('eq','text 1')
  cy.contains('a simple block test').should('be.visible')
})
})