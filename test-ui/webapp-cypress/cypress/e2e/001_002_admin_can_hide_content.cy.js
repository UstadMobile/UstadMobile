describe('The admin can hide content in Library', () => {
  it('Ustad Mobile - hide content test', () => {
  cy.login('admin','testpass')
  cy.addContent('/home/ustadmobile/StudioProjects/ustadmobile/test-ui/webapp-cypress/content/Epub_Content.epub')
  cy.wait(2000)
  cy.contains("Content001").parents("*[role='button']").trigger('mousedown');
  cy.wait(5000)
  cy.contains("Content001").parents("*[role='button']").trigger('mouseleave');
  cy.contains("visibility_off").click()
  cy.logout()
  cy.wait(5000)
  cy.login('admin','testpass')
  cy.contains('Library').click()
 })
})
