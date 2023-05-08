describe('The admin can add student and  student joins the class using link or code', () => {
  it('Ustad Mobile - add student and  student joins the class using link or code', () => {
  cy.startTestServer()
  cy.login('admin','testpass')
  cy.addStudent_Course('TestCourse','ajay','kumar','Male','ak','test')
  cy.addStudent_Course('TestCourse','sathya','varma','Male','sv','test')
  cy.logout()
  cy.login('sm','test')
  cy.teacher_record_attendance('TestCourse','sathya','varma')
  cy.wait(2000)
 })
})
