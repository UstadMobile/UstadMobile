describe('WEB_001_002_add_invalid_content', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

  it('Admin user create a course and add members to the course', () => {
  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.contains("Library").click()
  cy.contains("button","Content").click()
  cy.get('#new_content_from_file').click()
  cy.get('input[type="file"]')
    .selectFile('../test-files/content/Invalid_Video_Content.mp4',{force:true})
  cy.contains('Invalid file').should('exist')
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})