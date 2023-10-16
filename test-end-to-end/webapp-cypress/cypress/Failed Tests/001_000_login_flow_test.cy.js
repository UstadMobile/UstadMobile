describe('The Login flow Test', () => {
  it('Ustad Mobile - Login flow test', () => {

  cy.login('admin','testpass')
  cy.logout()
  cy.login('admin','testpass')
 })
})
