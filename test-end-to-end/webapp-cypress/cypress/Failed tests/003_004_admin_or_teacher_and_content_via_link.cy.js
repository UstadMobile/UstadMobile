describe('003_004_admin_or_teacher_and_content_via_link', () => {
it('Start Ustad Test Server ', () => {
  // Start Test Server
  cy.ustadStartTestServer()
})

it('User able to expand and collapse the module blocks', () => {
  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
  // Add a new course
  cy.ustadAddCourse('003_004')
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
   // Add Content from Link
    cy.contains('button','Edit').click()
    cy.contains("Add block").click()
    cy.contains("Content").click()
    cy.contains('Import from link').click()
   // http://prajwalftp.ustadmobile.com/upload/Ustad%20App%20contents%20-%202021/Ebooks/%e0%a4%86%e0%a4%ab%e0%a5%8d%e0%a4%a8%e0%a5%8b_%e0%a4%95%e0%a5%81%e0%a4%96%e0%a5%81%e0%a4%b0%e0%a4%be_%e0%a4%97%e0%a4%a8___%e0%a4%a8%e0%a5%87%e0%a4%aa%e0%a4%be%e0%a4%b2%e0%a5%80.epub
})
})