describe('001_002_add_invalid_content', () => {
 it('Start Ustad Test Server ', () => {
  // Start Test Server
    cy.ustadStartTestServer()
})
  it('Admin user create a course and add members to the course', () => {

  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.contains("Library").click()
  cy.contains("button","Content").click()
  cy.get('#new_content_from_file').click({force: true})
  cy.get('input[type="file"]')
      .selectFile('../test-files/content/Invalid_Video_Content.mp4',{force:true})
  //cy.ustadAddContentToLibrary(,'Content_001')
  cy.contains('Invalid file : Exception importing what looked like video').should('exist')

})
})