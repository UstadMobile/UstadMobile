describe('001_003_move_content', () => {
 it('Start Ustad Test Server ', () => {
  // Start Test Server
    cy.ustadStartTestServer()
})

it('Admin user move content to folder', () => {
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
 // Add H5p File
  cy.ustadAddContentToLibrary('../test-files/content/H5p_Content.h5p','Content_001')
  cy.contains('Content_001').click()
  cy.ustadOpenH5pEpub('Content_001')
  cy.ustadVerifyH5p()
  cy.go('back')
 // Add new folder in library
  cy.contains("Library").click()
  cy.ustadAddFolderToLibrary('Test folder')
 // Move content to new folder
  cy.contains("Library").click()
  cy.contains('Content_001').rightclick()
  cy.contains("Move to").click()
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