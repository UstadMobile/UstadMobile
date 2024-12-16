describe('WEB_001_010_content_attempt_list_test', () => {
 it('Start Ustad Test Server ', () => {
  // Start Test Server
    cy.ustadStartTestServer()
})

it('Admin user add content to the library', () => {
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})

 // Add Video Content
  cy.ustadAddContentToLibrary('../test-files/content/Video_Content.mp4','Content_003')
  cy.contains('Content_003').click()
  cy.contains("Importing").should("be.visible")
  cy.contains("Importing", { timeout: 20000 }).should("not.exist") //Wait for importing (conversion) to finish
  cy.contains("button","OPEN").click()
  cy.contains("#courseblock_title", "Content_003").should("be.visible")
  cy.ustadVerifyVideo()
// Add a new course
  cy.ustadAddCourse('Test course')
//Add a teacher
  cy.contains("button","Members").click()
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','1','Female')
// Add account for teacher
  cy.contains("Student 1").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('stud1','tests1')
})

it('Student user watch video content', () => {
  cy.ustadClearDbAndLogin('stud1','tests1',{timeout:8000})
  cy.contains('Content_003').click()
  cy.contains("button","OPEN").click()
  cy.contains("#courseblock_title", "Content_003").should("be.visible")
  cy.ustadVerifyVideo()
})

it('Student user watch video content', () => {
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.contains('Content_003').click()
  cy.contains("button","Attempts").click()
//Assert progress bar visible
  cy.contains("Student 1").click()
// Assert attempt score,completion,duration visible
})
})