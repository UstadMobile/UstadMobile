describe('001_003_move_content', () => {
 it('Start Ustad Test Server ', () => {
  // Start Test Server
    cy.ustadStartTestServer()
})

it('Admin user add content to the library', () => {
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
 // Add H5p File
  cy.ustadAddContentToLibrary('../test-files/content/H5p_Content.h5p','Content_001')
  cy.contains('Content_001').click()
  cy.contains('OPEN').click({force: true})
  cy.get('#appbar_title').should('be.visible').invoke('text').should('eq', 'Content_001');

 // Verify H5p Content
 /*
  https://www.lambdatest.com/blog/how-to-handle-iframes-in-cypress/
  The iframe reference is from the above link
 */
  cy.get('iframe')
  cy.get('#xapi_content_frame')
  .its('0.contentDocument')
  .its('body')
  .find('.h5p-iframe.h5p-initialized')
  .its('0.contentDocument')
  .its('body')
  .find(".h5p-question-check-answer.h5p-joubelui-button").click()
  cy.go('back')
 // Add new folder in library
  cy.ustadAddFolderToLibrary('Test folder')
 // Move content to new folder
  cy.contains("Library").click()
  cy.contains('Content_001').rightclick()
  cy.contains("Move to",{timeout:3000}).click()
  cy.contains("Test folder").click()
  cy.get('#select_folder_button').click()
  cy.contains("Test folder").click()
  cy.contains('Content_001').should('be.visible')
})

it('validating move content sync', () => {

  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.contains("Library").click()
  cy.contains("Test folder").should('be.visible')
  cy.contains("Test folder").click()
  cy.contains('Content_001').should('be.visible')

})
})