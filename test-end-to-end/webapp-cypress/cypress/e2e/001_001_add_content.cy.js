describe('001_001_add_content.cy.js ', () => {
 it('Start Ustad Test Server ', () => {
  // Start Test Server
    cy.ustadStartTestServer()
})
  it('Admin user create a course and add members to the course', () => {

  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.ustadAddContentToLibrary('/home/ustadmobile/Testfolder/test-end-to-end/test-files/content/H5p_Content.h5p','Content_001')
  cy.contains('Content_001').click()
  cy.contains('OPEN').click({force: true})
  cy.contains("Content_001").should('exist')

cy.wait(2000)
cy.get('iframe').then(($iframe) => {
const $body = $iframe.contents().find('body')
 $iframe.find('.h5p-true-false-answer').click()
 $iframe.find('.hp-question-check-answer').click()
})


/*

cy.get('iframe').iframe().then((iframe) => {
  iframe.find('.h5p-true-false-answer').click()
  iframe.find('.h5p-question-check-answer').click()
  //find('.h5p-question-introduction').should('exist'); // Checking if the text exists in the iframe body
});*/
cy.wait(2000)
  cy.go('back')
  cy.ustadAddContentToLibrary('/home/ustadmobile/Testfolder/test-end-to-end/test-files/content/Epub_Content.epub','Content_002')
  cy.contains('Content_002').click()
  cy.contains("button","OPEN").click()
  //cy.contains("Oliver Twist; or, The Parish Boy's Progress. Illustrated").should('exist')
  cy.go('back')
  cy.ustadAddContentToLibrary('/home/ustadmobile/Testfolder/test-end-to-end/test-files/content/Video_Content.mp4','Content_003')
  cy.contains('Content_003').click()
  cy.contains("button","OPEN").click()
  cy.contains('Content_003').should('exist')
})
})