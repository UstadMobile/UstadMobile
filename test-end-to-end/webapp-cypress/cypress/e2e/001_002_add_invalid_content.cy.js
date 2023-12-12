describe('001_001_add_content.cy.js ', () => {
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
      .selectFile('/home/ustadmobile/Testfolder/test-end-to-end/test-files/content/Invalid_Video_Content.mp4',{force:true})
  //cy.ustadAddContentToLibrary(,'Content_001')
  cy.contains('Invalid file : Exception importing what looked like video').should('exist')

})
})