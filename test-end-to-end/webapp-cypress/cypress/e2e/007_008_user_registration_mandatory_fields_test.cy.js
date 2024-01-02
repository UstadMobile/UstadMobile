describe('007_006_admin_enable_or_disable_guest_login', () => {
 it('Start Ustad Test Server ', () => {
  // Start Test Server
    cy.ustadStartTestServer()
})

it('Admin enable registration', () => {
  // Admin user login
   cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
   cy.get('#settings_button').click()
   cy.contains('Site').click()
   cy.contains('Edit').click()
  //https://docs.cypress.io/api/commands/should#Assert-the-href-attribute-is-equal-to-users
   cy.get('.ql-editor.ql-blank').should('have.attr', 'contenteditable').and('equal', 'true',{timeout:3000})
   cy.get('.ql-editor.ql-blank').clear().type("New Terms")
   cy.get('#registration_allowed').click({force:true})
   cy.get('#actionBarButton').should('be.visible')
   cy.get('#actionBarButton').click()
   cy.contains('Yes').should('exist')
   cy.get('#header_avatar').click()
   cy.contains('Add another account').click()
   cy.get('#create_account_button').should('be.visible')
   cy.get('#create_account_button').click()
   cy.ustadBirthDate(cy.get(".MuiInputBase-input.MuiOutlinedInput-input"), new Date("2010-06-01"));
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
  // verify gender field as blank
   cy.contains("label", "Last name").parent().find("input").clear().type('1')
   cy.get('div[id="gender"]').click()
   cy.contains("li","Unset").click()
   cy.contains('Register').click()
   cy.get('.Mui-error').contains('Gender').should('exist') //gender error
})
})