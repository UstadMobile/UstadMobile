describe('WEB_007_001_admin_enable_or_disable_user_registration', () => {

it('Start Ustad Test Server ', () => {
// Start Test Server
    cy.ustadStartTestServer()
 })

it('Admin enable registration', () => {
// Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.ustadEnableUserRegistration()
})

it('Verify New user registration is enabled', () => {
  cy.log('Clearing IndexedDB');
  cy.clearIndexedDb('localhost_8087') // clearing index db
  cy.visit('http://localhost:8087/', {timeout:60000})
  cy.contains('button[class*="MuiButton-outlinedPrimary"]', 'New user').click();
  cy.ustadBirthDate(cy.get("#age_date_of_birth"), new Date("2010-06-01"));
  cy.contains('button','Next').click()
  cy.contains('New Terms').should('be.visible')
  cy.get('#accept_button').should('be.visible')
})

it('Admin disable registration', () => {
// Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.get('#settings_button').click()
  cy.contains('Site').click()
  cy.contains('Edit').click()
//https://docs.cypress.io/api/commands/should#Assert-the-href-attribute-is-equal-to-users
  cy.get('#terms_html_edit .ql-editor').as('editor')
  cy.get('@editor').click().clear()
  cy.get('.Mui-checked.PrivateSwitchBase-root').should('exist') //verified registration_allowed switch is on
  cy.get('#registration_allowed').click({force:true})
  cy.get('.Mui-checked.PrivateSwitchBase-root').should('not.exist') //verified registration_allowed switch is off
  cy.get('#actionBarButton').click()
  cy.contains('No').should('exist')
})

it('Verify New user registration is disabled', () => {
   cy.log('Clearing IndexedDB');
    cy.clearIndexedDb('localhost_8087') // clearing index db
    cy.visit('http://localhost:8087/', {timeout:60000})
    cy.contains('button[class*="MuiButton-outlinedPrimary"]', 'New user').should('not.exist') // Verified new user registration is disabled
    cy.get('input#username', { timeout: 10000 }).should('exist')
})
})