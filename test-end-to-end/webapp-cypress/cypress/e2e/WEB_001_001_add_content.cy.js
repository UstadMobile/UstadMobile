describe('WEB_001_001_add_content', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Admin user add content to the library', () => {
 // Ensure LEARNING_SPACE_URL is defined before using it
    const learningSpaceUrl = Cypress.env('LEARNING_SPACE_URL')
    cy.log(`Learning Space URL: ${learningSpaceUrl}`)
    expect(learningSpaceUrl).to.exist

 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
 // Add H5p File
  cy.ustadAddContentToLibrary('../test-files/content/H5p_Content.h5p','Content_001')
  cy.contains('Content_001').click()
  cy.contains("Importing", { timeout: 20000 }).should("not.exist") //In case importing
  cy.ustadOpenH5P("Content_001")
  cy.ustadGetH5pBody().find(".h5p-question-check-answer.h5p-joubelui-button","Check").should("be.visible")
  cy.go('back')

 //Add Epub content
  cy.ustadAddContentToLibrary('../test-files/content/Epub_Content1.epub','Content_002')
  cy.contains('Content_002').click()
  cy.go('back')

 // Add Video Content
  cy.ustadAddContentToLibrary('../test-files/content/Video_Content.mp4','Content_003')
  cy.contains('Content_003').click()
  cy.contains("Importing").should("be.visible")
  cy.contains("Importing", { timeout: 20000 }).should("not.exist") //Wait for importing (conversion) to finish
  cy.contains("button","OPEN").click()
  cy.contains("#appbar_title", "Content_003").should("be.visible")
  cy.ustadVerifyVideo()

  cy.go('back')
  cy.go('back')
  cy.contains('Content_002').click()
  cy.ustadOpenH5pEpub('Content_002')
  cy.ustadVerifyEpub('THE ADOPTING OF ROSA MARIE')
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})