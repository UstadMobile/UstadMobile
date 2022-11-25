describe('The admin can add content to Library', () => {
  it('Ustad Mobile - add content test', () => {
  cy.login('admin','testpass')
  cy.addContent('/home/ustadmobile/StudioProjects/ustadmobile/test-ui/webapp-cypress/content/H5p_Content.h5p','Content002')
  cy.contains("Content002").parents("*[role='button']").click()
  cy.wait(4000) 
  cy.get("button[id='Y']").click()
  cy.wait(4000) // wait for 10 seconds
})
})
