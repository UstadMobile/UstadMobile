describe('WEB_007_008_user_registration_mandatory_fields_test', () => {
 it('Start Ustad Test Server ', () => {
  // Start Test Server
    cy.ustadStartTestServer()
})

it('Admin enable registration', () => {
  // Admin user login
   cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
   cy.ustadEnableUserRegistration()
   cy.get('#header_avatar').click()
   cy.contains('Add another account').click()
   cy.get('#create_account_button').should('be.visible')
   cy.get('#create_account_button').click()
   cy.ustadBirthDate(cy.get("#age_date_of_birth"), new Date("2010-06-01"));
   cy.contains('button','Next').click()
   cy.get('#accept_button').click()
   cy.contains('Register').click()
   cy.get('.Mui-error').contains('First names').should('exist') //Firstnames error
   cy.get('.Mui-error').contains('Last name').should('exist') //Lastname error
   cy.get('.Mui-error').contains('Gender').should('exist') //gender error
   cy.get('.Mui-error').contains('Username').should('exist') //username error
   cy.get('.Mui-error').contains('Password').should('exist') //password error
  // verify password field as blank
   cy.contains("label", "First names").parent().find("input").clear().type('student')
   cy.contains("label", "Last name").parent().find("input").clear().type('1')
   cy.get('div[id="gender"]').click()
   cy.contains("li","Female").click()
   cy.contains("label", "Username*").parent().find("input").clear().type('student1')
   cy.contains('Register').click()
   cy.get('.Mui-error').contains('Password').should('exist')
   cy.contains('This field is required').should('be.visible')
  // verify username field as blank
   cy.contains("label", "Username*").parent().find("input").clear()
   cy.contains("label", "Password").parent().find("input").clear().type('test1234')
   cy.contains('Register').click()
   cy.get('.Mui-error').contains('Username').should('exist') //username error
  // verify firstname field as blank
   cy.contains("label", "Username*").parent().find("input").clear().type('student1')
   cy.contains("label", "First names").parent().find("input").clear()
   cy.contains('Register').click()
  // verify lastname field as blank
   cy.contains("label", "First names").parent().find("input").clear().type('student')
   cy.contains("label", "Last name").parent().find("input").clear()
   cy.contains('Register').click()
   cy.get('.Mui-error').contains('Last name').should('exist') //Lastname error
   cy.contains("label", "Last name").parent().find("input").clear().type('1')
   cy.get('div[id="gender"]').click()
   cy.contains("li","Female").click()
   cy.contains('Register').click()
   cy.contains('Courses').should('be.visible')
})
})