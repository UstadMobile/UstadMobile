describe('The admin can add epub content to Library', () => {
  it('Ustad Mobile - add content test', () => {
  cy.login('admin','testpass')
  cy.addContent('/home/ustadmobile/StudioProjects/ustadmobile/test-end-to-end/test-files/content/Epub_Content.epub','Content001Epub')
  cy.contains("Content001Epub").parents("*[role='button']").click()
  cy.wait(4000) 
  cy.get("button[id='Y']").click()
  cy.wait(4000) // wait for 10 seconds
})
})
