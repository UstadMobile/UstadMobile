describe('The admin can add h5p content to Library', () => {
  it('Ustad Mobile - add content test', () => {
  cy.startTestServer()
  cy.login('admin','testpass')
  cy.addContent('/home/ustadmobile/StudioProjects/ustadmobile/test-end-to-end/test-files/content/H5p_Content.h5p','Content002H5p')
  cy.contains("Content002H5p").parents("*[role='button']").click()
  cy.wait(4000) 
  cy.get("button[id='Y']").click()
  cy.wait(4000) // wait for 10 seconds
})
})
