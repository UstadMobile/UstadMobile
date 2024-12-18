describe('WEB_001_010_content_attempt_list_test', () => {
 it('Start Ustad Test Server ', () => {
  // Start Test Server
    cy.ustadStartTestServer()
})

it('Admin user add content to the library', () => {
// Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
// Add a new course
   cy.ustadAddCourse('Test Course')
//Add a teacher
   cy.contains("button","Members").click()
   cy.contains("span","Add a teacher").click()
   cy.ustadAddNewPerson('Teacher','1','Female')
// Add account for teacher
   cy.contains("Teacher 1").click()
   cy.contains('View profile').click()
   cy.ustadCreateUserAccount('teach1','testt1')
//Add a student1
   cy.contains("span","Add a student").click()
   cy.ustadAddNewPerson('Student','1','Male')
   cy.contains("button","Members").should('be.visible')
//Add account for student1
   cy.contains("Student 1").click()
   cy.contains('View profile').click()
   cy.ustadCreateUserAccount('stud1','tests1')
//Add a student2
   cy.contains("span","Add a student").click()
   cy.ustadAddNewPerson('Student','2','Male')
   cy.contains("button","Members").should('be.visible')
//Add account for student1
   cy.contains("Student 2").click()
   cy.contains('View profile').click()
   cy.ustadCreateUserAccount('stud2','tests2')
// Add Content block
   cy.contains("Course").click()
   cy.contains("Test Course").click()
   cy.contains("button","Course").click()
   cy.contains("button","Edit").click()
   cy.contains("Add block").click()
   cy.get("#add_content_block").click()
   cy.contains('Import from file').click()
   cy.get('input[type="file"]')
     .selectFile('../test-files/content/Video_Content.mp4',{force: true})

//Continue import
   cy.contains('#actionBarButton', 'Next').click()

//Set CourseBlock title
   cy.contains("#appbar_title", "Edit content block").should("be.visible")
   cy.get('input[id="title"]').click()
   cy.get('input[id="title"]').clear().type('Content_001',{timeout: 2000})
   cy.contains('#actionBarButton', 'Done').click()
   cy.contains("button","Save").click()
   cy.contains('button','Edit').should('exist')
   cy.contains("Content_001").click()
   cy.contains("Importing").should("be.visible")
   cy.contains("Importing", { timeout: 20000 }).should("not.exist") /
   cy.contains("button","OPEN").click()
   cy.go('back')
   cy.go('back')
   cy.go('back')
// Add Content block
   cy.contains("Course").click()
   cy.contains("Test Course").click()
   cy.contains("button","Course").click()
   cy.contains("button","Edit").click()
   cy.contains("Add block").click()
   cy.get("#add_content_block").click()
   cy.contains('Import from file').click()
   cy.get('input[type="file"]')
     .selectFile('../test-files/content/H5p_Content.h5p',{force: true})
//Continue import
   cy.contains('#actionBarButton', 'Next').click()
//Set CourseBlock title
   cy.contains("#appbar_title", "Edit content block").should("be.visible")
   cy.get('input[id="title"]').click()
   cy.get('input[id="title"]').clear().type('Content_002',{timeout: 2000})
   cy.contains('#actionBarButton', 'Done').click()
   cy.contains("button","Save").click()
   cy.contains('button','Edit').should('exist')
   cy.contains("Content_002").click()
   cy.contains("button","OPEN").click()
   cy.go('back')
   cy.go('back')
 })


it('Student user watch video content', () => {
  cy.ustadClearDbAndLogin('stud1','tests1',{timeout:8000})
  cy.contains('Test Course').click()
  cy.contains("Content_001").click()
  cy.contains("button","OPEN").click()
  cy.get('video[src^="http://localhost:8087"]',{timeout:8000}).click();
  cy.get('video[src^="http://localhost:8087"]',{timeout:8000}).click();
  cy.go('back')
  cy.contains("button","Attempts").click()
//Assert progress bar visible
  cy.contains('0% Completion').should('exist')
  cy.contains("Student 1").click()
// Assert attempt score,completion,duration visible
  cy.contains('Incomplete').should('exist')
  cy.contains('0% Completion').should('exist')
})
})