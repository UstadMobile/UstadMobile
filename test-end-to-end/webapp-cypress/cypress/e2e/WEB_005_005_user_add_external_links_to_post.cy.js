/*
 * Unfortunately, Cypress does not allow opening links in a new tab. The workaround was to remove
 * the target attribute. There will need to be some workaround added to the JS code.
 */

describe('005_005_user_add_external_links_to_post', () => {
it('Start Ustad Test Server ', () => {
  // Start Test Server
  cy.ustadStartTestServer()
})

it('Admin add discussion board and post', () => {
  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
  // Add a new course
  cy.ustadAddCourse('005_005')
  // Add discussion board
  cy.contains('button','Edit').click()
  cy.ustadAddDiscussionBoard('Discussion 1')
  cy.contains('Edit course').should('be.visible')
  cy.contains("button","Save").click()
  // Add post to the discussion
  cy.contains('005_005').should('be.visible')
  cy.contains('Discussion 1').click()
  cy.contains('Post').click()
  cy.get('#discussion_post_title').type('Topic 1')
  cy.get('.ql-editor').ustadTypeAndVerify('Discusssion post')
  cy.get('#actionBarButton').click()
  cy.go('back')
  cy.go('back')
  //Add a teacher
  cy.contains("button","Members").click()
  cy.contains("span","Add a teacher").click()
  cy.ustadAddNewPerson('Teacher','A','Female')
  // Add account for teacher
  cy.contains("Teacher A").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('teacherA','test1234')
  //Add a student
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','A','Male')
  cy.contains("button","Members").should('be.visible')
  //Add account for student
  cy.contains("Student A").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('studentA','test1234')
})

it('Teacher able to add external link as reply to the post', () => {
  // Teacher Login
  cy.ustadClearDbAndLogin('teacherA','test1234')
  cy.contains("Courses").should('be.visible')
  cy.contains('005_005').click()
  // Add reply to the post board
  cy.contains('Discussion 1').click()
  cy.contains('Topic 1').click()
  cy.get('[data-placeholder="Add a reply"]').type('External link');
  cy.get('[data-placeholder="Add a reply"]').type('{selectall}')
  cy.get('.ql-link').click()
  cy.get('[data-video="Embed URL"]').type('https://github.com/UstadMobile/UstadMobile/blob/primary/test-end-to-end/README.md')
  cy.get('[class=ql-action]').click()
  cy.contains('button','Post').click()
  cy.contains('External link').should('be.visible')
  cy.go('back')
  cy.go('back')
})

it('Student able to open the external link in the reply', () => {
  // Student Login
  cy.ustadClearDbAndLogin('studentA','test1234')
  cy.contains("Courses").should('be.visible')
  cy.contains('005_005').click()
  // Open link on the post board
  cy.contains('Discussion 1').click()
  cy.contains('Topic 1').click()
  cy.contains('External link').invoke('attr','target', '_self').click()
  cy.url().should('include','https://github.com/UstadMobile/UstadMobile/blob/primary/test-end-to-end/README.md')
})
})