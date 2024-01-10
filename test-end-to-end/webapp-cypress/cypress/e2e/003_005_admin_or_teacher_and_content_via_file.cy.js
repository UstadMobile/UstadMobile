describe('003_005_admin_or_teacher_and_content_via_file', () => {
it('Start Ustad Test Server ', () => {
  // Start Test Server
  cy.ustadStartTestServer()
})

it('Admin able to add content block from library', () => {
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
  cy.ustadAddContentToLibrary('../test-files/content/H5p_Content.h5p','Content_001')
 // Add a new course
  cy.contains("Courses").click()
  cy.ustadAddCourse('003_005')
 //Add a teacher
  cy.contains("button","Members").click()
  cy.contains("span","Add a teacher").click()
  cy.ustadAddNewPerson('Teacher','1','Female')
 // Add account for teacher
  cy.contains("Teacher 1").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('teacher1','test1234')
 // Add module block
  cy.contains('button','Course').click()
  cy.contains('button','Edit').click()
  cy.ustadAddModuleBlock('module 1')
 // Add Content from library
  cy.contains('button','Edit').click()
  cy.contains("Add block").click()
  cy.contains("Content").click()
  cy.get('#content_entry_filter_chip_box').contains('Library').click()
  cy.contains("Content_001").click()
  cy.contains("Done").click()
  cy.contains("Save").click()
  cy.contains('button','Edit').should('exist')
})

it('Teacher able to add content block from file', () => {
  cy.ustadClearDbAndLogin('teacher1','test1234')
 // Add Assignment block
  cy.contains("Course").click()
  cy.contains("003_005").click()
  cy.contains("button","Course").click()
  cy.contains("button","Edit").click()
  cy.contains("Add block").click()
  cy.get("#add_content_block").click()
  cy.contains('Import from file').click()
  cy.get('input[type="file"]')
    .selectFile('../test-files/content/Epub_Content1.epub',{force:true})
  cy.get('input[id="title"]').click()
  cy.get('input[id="title"]').clear().type('Content_002',{timeout: 2000})
  cy.get('#actionBarButton').click()
  cy.contains("button","Save").click()
  cy.contains('button','Edit').should('exist')
  cy.contains("Content_001").click()
  cy.contains("OPEN").click({timeout:2000})
  cy.get('#appbar_title').should('be.visible').invoke('text').should('eq', 'Content_001')
 /*****  Verify H5p Content

      https://www.lambdatest.com/blog/how-to-handle-iframes-in-cypress/
      The iframe reference is from the above link
 *****/
  cy.get('iframe')
  cy.get('#xapi_content_frame')
    .its('0.contentDocument')
    .its('body')
    .find('.h5p-iframe.h5p-initialized')
    .its('0.contentDocument')
    .its('body')
    .find(".h5p-question-check-answer.h5p-joubelui-button").click()
  cy.go('back')
  cy.go('back')
 // Verify Epub content
  cy.contains('Content_002').click()
  cy.get('#appbar_title').should('be.visible').invoke('text').should('eq','The Adopting of Rosa Marie / (A Sequel to Dandelion Cottage)')
  cy.contains("button","OPEN",{timeout: 5000}).click()
  cy.get('#header_overflow_menu_expand_button').click()
  cy.contains('Table of contents').click()
  cy.contains('THE ADOPTING OF ROSA MARIE').should('exist')
})
})