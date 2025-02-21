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
  cy.contains("Library").click()
  cy.contains("Content_001").click()
  cy.contains("Attempts").click()
 // cy.contains("Admin User").should("exist") -------- Attempts are not visible for this h5p content on cypress

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
      $video[0].play();
    });

  cy.wait(11000); // Wait for 11 seconds
  cy.get('video')
    .should('have.prop', 'ended', true);
  cy.go('back')
 // attempts made on video
  cy.contains("Attempts").click()
  cy.contains("Admin User").should("exist")
  cy.contains("Attempts: 1").should("exist")
  cy.contains("100% Completion").should("exist")
  cy.contains("Admin User").click()
  cy.contains("Completed").should("exist")
  cy.get('svg[data-testid="TimerIcon"]').should('exist');
  cy.contains("100% Completion").should("exist")
  cy.get('.MuiStack-root')
    .parent() // Get the parent container
    .find('span[role="progressbar"]')
    .should('exist');
  cy.contains("Library").click()
  cy.contains('Content_002').click()
  cy.ustadOpenH5pEpub('Content_002')
  cy.ustadVerifyEpub('THE ADOPTING OF ROSA MARIE')
  cy.ustadVerifyEpub('The Adopting of Rosa Marie / (A Sequel to Dandelion Cottage)');
  cy.contains("THE ADOPTING OF ROSA MARIE").click()
// Attempts made on epub
  cy.contains("Library").click()
  cy.contains("Content_002").click()
  cy.contains("Attempts").click()
  cy.contains("Admin User").should("exist")
  cy.contains("Admin User").click()
  cy.contains("Progressed").click()
   cy.get('.MuiStack-root')
     .parent() // Get the parent container
     .find('span[role="progressbar"]')
     .should('exist');
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})