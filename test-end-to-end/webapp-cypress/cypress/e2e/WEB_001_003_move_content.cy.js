describe('WEB_001_003_move_content', () => {
 it('Start Ustad Test Server ', () => {
  // Start Test Server
    cy.ustadStartTestServer()
})

it('Admin user move content to folder', () => {
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
 // Add H5p File
  cy.ustadAddContentToLibrary('../test-files/content/H5p_Content.h5p','Content_001')
  cy.ustadAddFolderToLibrary('Test folder')
 // Move content to new folder
  cy.contains("Library").click()
  cy.contains('Content_001').rightclick()
  cy.contains("Move to").click()

  //Ensure that we have moved into selecting a move destination mode
  cy.get("#select_folder_button").should("be.visible")
  cy.contains("Test folder").click()
  //verify that we moved to the given folder
  cy.contains("#appbar_title", "Test folder").should("be.visible")
  cy.get('#select_folder_button').click()

  cy.contains("#appbar_title", "Library").should("be.visible")
  cy.contains("Test folder").click()
  cy.contains('Content_001').should('be.visible')
})

it('Validate move content synced', () => {

  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.contains("Library").click()
  cy.contains("Test folder").should('be.visible')
  cy.contains("Test folder").click()
  cy.contains('Content_001').should('be.visible')

})
})