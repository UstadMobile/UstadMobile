/*
 * Unfortunately, Cypress does not allow opening links in a new tab. The workaround was to remove
 * the target attribute. There will need to be some workaround added to the JS code.
 */

describe('WEB_005_004_user_add_external_links_to_post', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Admin add discussion board and post', () => {
  cy.importUsersViaHttp("Ustad_Teacher_and_Students.csv");
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
 // Add discussion board
  cy.contains('button','Edit').click()
  cy.ustadAddDiscussionBoard('Discussion 1')
  cy.contains('Edit course').should('be.visible')
  cy.contains("button","Save").click()
  // Add post to the discussion
  cy.contains('Test Course Block').should('be.visible')
  cy.contains('Discussion 1').click()
  cy.contains('Post').click()
  cy.get('#discussion_post_title').type('Topic 1')
  cy.get('.ql-editor').ustadTypeAndVerify('Discusssion post')
  cy.get('#actionBarButton').click()
  cy.contains("Topic 1", { timeout: 10000 }).should('be.visible')
})

it('Teacher able to add external link as reply to the post', () => {
  // Teacher Login
  cy.ustadClearDbAndLogin('teach1','testt1')
  cy.contains("Courses").should('be.visible')
  cy.contains('Test Course Block').click()
  // Add reply to the post board
  cy.contains('Discussion 1').click()
  cy.contains('Topic 1').click()
  cy.get('[data-placeholder="Add a reply"]').type('External link');
  cy.get('[data-placeholder="Add a reply"]').type('{selectall}')
  cy.get('.ql-link').click()
  cy.get('[data-video="Embed URL"]').type(`${Cypress.config('baseUrl')}testcontroller/test-files/content/example.html`)
  cy.get('[class=ql-action]').click()
  cy.contains('button','Post').click()
  cy.contains('External link', { timeout: 10000 }).should('be.visible')
  cy.contains("Topic 1", { timeout: 10000 }).should('be.visible')
})

it('Student able to open the external link in the reply', () => {
  // Student Login
  cy.ustadClearDbAndLogin('stud1','tests1')
  cy.contains("Courses").should('be.visible')
  cy.contains('Test Course Block').click()
  // Open link on the post board
  cy.contains('Discussion 1').click()
  cy.contains('Topic 1').click()
  cy.contains('External link').invoke('attr','target', '_self').click()
  cy.url().should('include',`${Cypress.config('baseUrl')}testcontroller/test-files/content/example.html`)
  cy.contains('Welcome to Ustad Test Page').should('be.visible')
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})