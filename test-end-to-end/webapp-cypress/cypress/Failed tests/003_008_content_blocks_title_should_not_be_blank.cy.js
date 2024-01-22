describe('003_006_user_able_to_expand _and_collapse_modules', () => {
it('Start Ustad Test Server ', () => {
  // Start Test Server
  cy.ustadStartTestServer()
})

it('User able to expand and collapse the module blocks', () => {
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
 // Add a new course
  cy.ustadAddCourse('003_002')
 // Add module block
  cy.contains('button','Edit').click()
  cy.contains("Add block").click()
  cy.contains("Module").click()
 // Testing Module block - title as blank
  cy.contains("button","Done").click()
  cy.get('.MuiInputBase-colorPrimary.Mui-error').should('exist')
  cy.get('input[id="title"]').type('moduleTitle')
  cy.contains("button","Done").click()
  cy.contains('moduleTitle').should('be.visible')
  cy.contains("button","Save").click()
 // Add text block
  cy.contains('button','Edit').click()
  cy.contains("Add block").click()
  cy.contains("Text").click()
 // Testing Text block - title as blank
  cy.contains("button","Done").click()
  cy.get('.MuiInputBase-colorPrimary.Mui-error').should('exist')
  cy.get('input[id="title"]').type('textTitle')
  cy.contains("button","Done").click()
  cy.contains('textTitle').should('be.visible')
 // Add Assignment block
  cy.contains("Add block").should('be.visible').click()
  cy.contains("Assignment").click()
 // Testing Assignment block - title as blank
  cy.contains("button","Done").click()
  cy.get('.MuiInputBase-colorPrimary.Mui-error').should('exist')
  cy.get('input[id="title"]').type('assignmentTitle')
  cy.contains("button","Done").click()
  cy.contains('assignmentTitle').should('be.visible')
 // Add Discussion board
  cy.contains("Add block").click()
  cy.contains("Discussion board").click()
 //  Testing Discussion board - title as blank
  cy.contains("button","Done").click()
  cy.get('.MuiInputBase-colorPrimary.Mui-error').should('exist')
  cy.get('input[id="title"]').type('discussionTitle')
  cy.contains("button","Done").click()
  cy.contains('assignmentTitle').should('be.visible')
 // Add Content Block
  cy.contains("Add block").click()
  cy.get("#add_content_block").click()
  cy.contains('Import from file').click()
  cy.get('input[type="file"]')
    .selectFile('../test-files/content/Epub_Content1.epub',{force:true})
  cy.get('input[id="title"]').clear()
  cy.get('#actionBarButton').click()
  cy.get('.MuiInputBase-colorPrimary.Mui-error').should('exist')
  cy.get('input[id="title"]').click()
  cy.get('input[id="title"]').clear().type('Content_002',{timeout: 2000})
  cy.get('#actionBarButton').click()
  cy.contains("button","Save").click()
  cy.contains('Content_002').should('be.visible')
})
})