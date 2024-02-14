describe('001_004_delete_content', () => {
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
 // Add Video Content
  cy.ustadAddContentToLibrary('../test-files/content/Video_Content.mp4','Content_003')
  cy.contains('Content_003').click()
  cy.contains("button","OPEN").click()
  cy.get('#appbar_title').should('be.visible').invoke('text').should('eq', 'Content_003');
 // Verify video content (duration > 0)
  cy.get('video').should(($video) => {
      expect($video[0].duration).to.be.gt(0)
  })
 // Delete content
  cy.contains("Library").click()
  cy.contains('Content_003').rightclick()
  cy.contains("Delete",{timeout:3000}).click()
  cy.contains('Content_003').should('not.exist')
  /*cy.contains("Test folder").click()
  cy.get('#select_folder_button').click()
  cy.contains("Test folder").click()
  cy.contains('Content_001').should('be.visible')*/
})

it('validating delete content', () => {

  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.contains("Library").click()
  cy.contains('Content_003').should('not.exist')

})

it('Admin restoring the deleted content ', () => {

  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.get('#settings_button').click()
  cy.contains('Deleted items').click()
  cy.get("button[aria-label='Restore']").click()
  cy.contains("Library").click()
  cy.contains('Content_003').should('be.visible')

})
it('validating restoring deleted content', () => {

  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.contains("Library").click()
  cy.contains('Content_003').should('be.visible')

})

it('Admin permanently delete the content ', () => {

  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  // Delete content
    cy.contains("Library").click()
    cy.contains('Content_001').rightclick()
    cy.contains("Delete",{timeout:3000}).click()
    cy.contains('Content_001').should('not.exist')

  cy.get('#settings_button').click()
  cy.contains('Deleted items').click()
  cy.get("button[aria-label='Delete permanently']").click()
  cy.contains('Confirm').click()
  cy.contains("Library").click()
  cy.contains('Content_001').should('not.exist')

})
})