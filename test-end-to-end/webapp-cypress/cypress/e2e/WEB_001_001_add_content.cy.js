describe('WEB_001_001_add_content', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Admin user add content to the library', () => {
// Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
// Add H5p File
  cy.ustadAddContentToLibrary('../test-files/content/H5p_Content.h5p','Content_001')
  cy.contains('Content_001').click()
  cy.contains("Importing", { timeout: 20000 }).should("not.exist") //In case importing
  cy.ustadOpenH5P("Content_001")
  cy.ustadGetH5pBody().find(".h5p-question-check-answer.h5p-joubelui-button","Check").should("be.visible")
  cy.ustadGetH5pBody().find(".h5p-true-false-answer").contains("Yes").click()
  cy.ustadGetH5pBody().find(".h5p-question-check-answer.h5p-joubelui-button","Check").click()
  cy.ustadGetH5pBody().find(".h5p-question-feedback-content-text","You got 1 out of 1 points").should("be.visible")
  cy.go('back')
// Attempts are not visible for this h5p content on cypress, manually it works

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
  cy.get('video')
    .then($video => {
      $video[0].play()
    })
  cy.get('video', { timeout: 15000 })
    .should('have.attr', 'data-ustad-video-state', 'ended')
  cy.go('back')
// attempts made on video
  cy.contains("Attempts").click()
  cy.get("#appbar_title").contains("Content_003").should("exist")
  cy.contains("Admin User : Completed").should("exist")
  cy.contains("100%").should("exist")
  cy.contains("Progress").should("exist")
  cy.get('span[role="progressbar"]').should('exist')
  cy.contains("Admin User : Completed").click()
  cy.contains("Completed").should("exist")
  cy.contains("100%").should("exist")
  cy.contains("Progress").should("exist")
  cy.get("#appbar_title").contains("Admin User - Content_003").should("exist")
  cy.get("svg[data-testid='CalendarTodayIcon']").should('exist')
  cy.get('span[role="progressbar"]').should('exist')
  cy.contains("Completed").click()
  cy.get("#appbar_title").contains("Content_003").should("exist")
  cy.get('span[role="progressbar"]').should('exist')
  cy.get("svg[data-testid='CheckIcon']").should('exist') // Filter already applied by default
  cy.get(".MuiChip-labelMedium").contains("Completed").should("exist")
  cy.get(".MuiListItemText-primary").contains("Completed Content_003").should("exist")
  cy.contains("100%").should("exist")
  cy.contains("Progress").should("exist")
// User making attempt on Epub content
  cy.contains("Library").click()
  cy.contains('Content_002').click()
  cy.contains('OPEN', { timeout: 20000 }).click()
  cy.ustadVerifyEpub('THE ADOPTING OF ROSA MARIE')
  cy.contains("THE ADOPTING OF ROSA MARIE").click()
// Attempts made on epub
  cy.contains("Library").click()
  cy.contains("Content_002").click()
  cy.contains("Attempts").click()
  cy.get("#appbar_title").contains("Content_002").should("exist")
  cy.contains("Admin User : Incomplete").should("exist")
  cy.get('span[role="progressbar"]').should('exist')
  cy.contains("Admin User : Incomplete").click()
  cy.contains("Incomplete").should('exist')
  cy.get("#appbar_title").contains("Admin User - Content_002").should("exist")
  cy.get('span[role="progressbar"]').should('exist')
  cy.contains("Incomplete").click()
  cy.get("#appbar_title").contains("Content_002").should("exist")
  cy.get("svg[data-testid='CheckIcon']").should('exist') // Filter already applied by default
  cy.get('span[role="progressbar"]').should('exist')
  cy.get("svg[data-testid='CheckIcon']").should('exist')
  cy.get(".MuiChip-labelMedium").contains("Progressed").should('exist')
  cy.get(".MuiListItemText-primary").contains("Progressed The Adopting of Rosa Marie / (A Sequel to Dandelion Cottage)").should("exist")
  cy.get(".MuiChip-labelMedium").contains("Progressed").click() // testing filter chip
  cy.get('span[role="progressbar"]').should('not.exist')
  cy.get("svg[data-testid='CheckIcon']").should('not.exist')

// Add Pdf Content
  cy.ustadAddContentToLibrary('../test-files/content/Pdf_Content.pdf','Content_004')
  cy.contains('Content_004').click()
  cy.contains("Importing", { timeout: 20000 }).should("not.exist")
  cy.contains("button","OPEN").click()
  cy.contains("#appbar_title", "Content_004").should("be.visible")
  cy.wait(3000)
  cy.go('back')
// Attempts made on pdf
  cy.contains("Attempts").click()
  cy.get("#appbar_title").contains("Content_004").should("exist")
  cy.contains("Admin User : Completed").should("exist")
  cy.contains("100%").should("exist")
  cy.contains("Progress").should("exist")
  cy.get('span[role="progressbar"]').should('exist')
  cy.contains("Admin User : Completed").click()
  cy.contains("Completed").should("exist")
  cy.contains("100%").should("exist")
  cy.contains("Progress").should("exist")
  cy.get("#appbar_title").contains("Admin User - Content_004").should("exist")
  cy.get("svg[data-testid='CalendarTodayIcon']").should('exist')
  cy.get('span[role="progressbar"]').should('exist')
  cy.contains("Completed").click()
  cy.get("#appbar_title").contains("Content_004").should("exist")
  cy.get('span[role="progressbar"]').should('exist')
  cy.get("svg[data-testid='CheckIcon']").should('exist') // Filter already applied by default
  cy.get(".MuiChip-labelMedium").contains("Completed").should("exist")
  cy.get(".MuiListItemText-primary").contains("Completed ").should("exist")
  cy.contains("100%").should("exist")
  cy.contains("Progress").should("exist")
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer()
  })
})