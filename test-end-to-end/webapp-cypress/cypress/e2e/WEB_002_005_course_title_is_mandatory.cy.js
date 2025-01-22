describe('WEB_002_005_course_title_is_mandatory', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

  it('Admin user create a course without title', () => {

  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.contains("Courses").should('be.visible')  // Assertion to check the user logged in successfully

 // Add a new course
  cy.contains("Courses").click()
  cy.contains("button","Course").click()
  cy.contains("Add a new course").click()
  cy.contains("button","Save").click()
  cy.get('.MuiInputBase-colorPrimary.Mui-error').should('exist')
  cy.get('input[id="clazz_name"]').type('courseName')
  cy.contains("button","Save").click()
  cy.contains("button","Edit").should('be.visible')
  cy.get('#appbar_title').should('be.visible').invoke('text').should('eq','courseName')
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})