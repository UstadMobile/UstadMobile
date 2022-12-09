describe('The admin can add video content to Library', () => {
  it('Ustad Mobile - add content test', () => {
  cy.startTestServer()
  cy.login('admin','testpass')
  cy.addContent('/home/ustadmobile/StudioProjects/ustadmobile/test-end-to-end/test-files/content/Video_Content.mp4','Content003Video')
  cy.contains("Content003Video").parents("*[role='button']").click()
  cy.wait(4000) 
  cy.get("button[id='Y']").click()
  cy.wait(4000) // wait for 10 seconds
})
})
