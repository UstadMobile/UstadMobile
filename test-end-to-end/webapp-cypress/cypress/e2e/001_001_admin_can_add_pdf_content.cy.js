describe('The admin can add h5p content to Library', () => {
  it('Ustad Mobile - add content test', () => {
  cy.startTestServer()
  cy.login('admin','testpass')
  cy.addContent('/home/ustadmobile/StudioProjects/ustadmobile/test-end-to-end/test-files/content/Pdf_Content.pdf','Content004Pdf')
  cy.contains("Content004Pdf").parents("*[role='button']").click()
  cy.wait(4000) 
  cy.get("button[id='Y']").click()
  cy.wait(4000) // wait for 10 seconds
})
})
