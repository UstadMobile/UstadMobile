describe('The admin can move content in Library', () => {
  it('Ustad Mobile - move content test', () => {
  cy.startTestServer()
  cy.login('admin','testpass')
  cy.addContent('/home/ustadmobile/StudioProjects/ustadmobile/test-end-to-end/test-files/content/Epub_Content.epub',"moveContentTest")
  cy.wait(2000)
  cy.contains("moveContentTest").parents("*[role='button']").trigger('mousedown');
  cy.wait(5000)
  cy.contains("moveContentTest").parents("*[role='button']").trigger('mouseleave');
  cy.contains("drive_file_move").click()
  cy.contains('Add new content').click()
  cy.contains("New folder").click()
  cy.contains("label", "Title").parent().find("input").clear().type('newFolder')
  cy.contains('Done').click()
  cy.wait(2000)
  cy.logout()
  //cy.wait(5000)
  //cy.login('admin','testpass')
  //cy.contains('Library').click()
 })
})
