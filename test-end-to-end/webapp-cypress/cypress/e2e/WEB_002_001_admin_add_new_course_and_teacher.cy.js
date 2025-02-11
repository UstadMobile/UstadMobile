describe('WEB_002_001_admin_add_new_course_and_teacher ', () => {
  before(() => {
     // Start Test Server
     cy.ustadStartTestServer(6000)
  })

it('Admin user create a course and add members to the course', () => {
// Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
// verify course title is mandatory
  cy.contains("Courses").click()
  cy.contains("button","Course").click()
  cy.contains("Add a new course").click()
  cy.contains("button","Save").click()
  cy.get('.MuiInputBase-colorPrimary.Mui-error').should('exist')
  cy.get('input[id="clazz_name"]').type('Test Course')
// Adding course banner pic
  cy.get('svg[data-testid="AddAPhotoIcon"]').click()
  cy.get('input[type="file"]').selectFile('../test-files/content/courseBannerPic.jpg',{force:true})
  cy.contains("button","Save").click()
// Add a teacher
  cy.contains("button","Members").click()
  cy.contains("span","Add a teacher").click()
  cy.ustadAddNewPerson('Teacher','1','Female')
 //Add a student
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','1','Male')
  cy.contains("button","Members").should('be.visible')
 // Add account for teacher
  cy.contains("Teacher 1").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('teach1','testt1')
 //Add account for student
  cy.contains("Student 1").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('stud1','tests1')

})

it('Teacher able to login to the app', () => {
 // Teacher Login
  cy.ustadClearDbAndLogin('teach1','testt1')
  cy.contains("Courses").should('be.visible')  // Assertion to check the user logged in successfully
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})