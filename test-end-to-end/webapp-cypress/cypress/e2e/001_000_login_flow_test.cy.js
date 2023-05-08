describe('The Login flow Test', () => {
  it('Ustad Mobile - Login flow test', () => {
  cy.startTestServer()
  cy.login('admin','testpass')
  cy.wait(5000)
  cy.logout()
  cy.wait(2000)
  cy.login('admin','testpass')
  cy.contains('Library').click()
 })
})
