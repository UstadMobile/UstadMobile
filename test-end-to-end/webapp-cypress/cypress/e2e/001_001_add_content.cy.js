describe('001_001_add_content.cy.js ', () => {
 it('Start Ustad Test Server ', () => {
  // Start Test Server
    cy.ustadStartTestServer()
})

it('Admin user add content to the library', () => {

  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  // Add H5p File
  cy.ustadAddContentToLibrary('/home/ustadmobile/Testfolder/test-end-to-end/test-files/content/H5p_Content.h5p','Content_001')
  cy.contains('Content_001').click()
  cy.contains('OPEN').click({force: true})
  cy.get('#appbar_title').should('be.visible').invoke('text').should('eq', 'Content_001');

 // Verify H5p Content
 /*
  https://www.lambdatest.com/blog/how-to-handle-iframes-in-cypress/
  The iframe reference is from the above link
 */
  cy.get('iframe')
  cy.get('.css-1pc9cj7')
  .its('0.contentDocument')
  .its('body')
  .find('.h5p-iframe.h5p-initialized')
  .its('0.contentDocument')
  .its('body')
  .find(".h5p-question-check-answer.h5p-joubelui-button").click()
  cy.go('back')
 //Add Epub content
  cy.ustadAddContentToLibrary('/home/ustadmobile/Testfolder/test-end-to-end/test-files/content/Epub_Content1.epub','Content_002')
  cy.contains('Content_002').click()
  cy.go('back')
 // Add Video Content
  cy.ustadAddContentToLibrary('/home/ustadmobile/Testfolder/test-end-to-end/test-files/content/Video_Content.mp4','Content_003')
  cy.contains('Content_003').click()
  cy.contains("button","OPEN").click()
  cy.get('#appbar_title').should('be.visible').invoke('text').should('eq', 'Content_003');
 // Verify video content (duration > 0)
  cy.get('video').should(($video) => {
      expect($video[0].duration).to.be.gt(0)
  })
 // Verify Epub content
  cy.go('back')
  cy.go('back')
  cy.contains('Content_002').click()
  cy.contains("button","OPEN").click()
  cy.get('#appbar_title').should('be.visible').invoke('text').should('eq','The Adopting of Rosa Marie / (A Sequel to Dandelion Cottage)')
  cy.get('#header_overflow_menu_expand_button').click()
  cy.contains('Table of contents').click()
  cy.contains('THE ADOPTING OF ROSA MARIE').should('exist')
})
})