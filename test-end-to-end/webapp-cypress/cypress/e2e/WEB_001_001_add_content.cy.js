describe('001_001_add_content', () => {
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
  cy.ustadOpenH5pEpub('Content_001')
  cy.ustadVerifyH5p()
  cy.go('back')
 //Add Epub content
  cy.ustadAddContentToLibrary('../test-files/content/Epub_Content1.epub','Content_002')
  cy.contains('Content_002').click()
  cy.go('back')
 // Add Video Content
  cy.ustadAddContentToLibrary('../test-files/content/Video_Content.mp4','Content_003')
  cy.contains('Content_003').click()
  cy.contains("button","OPEN").click()
  cy.contains("#appbar_title", "Content_003").should("be.visible")
  cy.ustadVerifyVideo()
  cy.go('back')
  cy.go('back')
  cy.contains('Content_002').click()
  cy.ustadOpenH5pEpub('Content_002')
  cy.ustadVerifyEpub()
})
})