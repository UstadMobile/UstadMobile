
// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add('login', (email, password) => { ... })


//below command added as per : https://github.com/thisdot/open-source/blob/main/libs/cypress-indexeddb/README.md
import '@this-dot/cypress-indexeddb';

// Start Test Server
Cypress.Commands.add('ustadStartTestServer', () => {
  cy.visit('http://localhost:8075/start'); // Use cy.visit to navigate to the start page
  cy.wait(6000); // Wait for 6 seconds after visiting the start page
});


//User Login
Cypress.Commands.add('ustadClearDbAndLogin', (username, password) => {

//below command added as per : https://github.com/thisdot/open-source/blob/main/libs/cypress-indexeddb/README.md
  cy.log('Clearing IndexedDB');
  cy.clearIndexedDb('localhost_8087') // clearing index db
// Adding query parameters on the url- below command added as per - https://docs.cypress.io/api/commands/visit#Add-query-parameters
  cy.visit('http://localhost:8087/', {timeout:60000},{
    qs: {
      username,
      password,
    },

  })
  cy.get('input#username', { timeout: 10000 }).should('exist').type(username) // 10 seconds
  cy.get('input#password').type(password)
  cy.get('button#login_button').click()
});

// Logout Flow

Cypress.Commands.add('ustadLogout', () => {
   cy.get('#header_avatar').click()
   cy.contains('LOG OUT').click()
})

// Add a content to Library
Cypress.Commands.add('ustadAddContentToLibrary',(contentPath,contentName) => {
  cy.contains("Library").click()
  cy.contains("button","Content").click()
  cy.get('#new_content_from_file').click({force: true})
  cy.get('input[type="file"]').selectFile(contentPath,{force:true})
  cy.get('input[id="content_title"]').click()
  cy.get('input[id="content_title"]').clear().type(contentName,{timeout: 2000})
  cy.get('#actionBarButton').click()

})

// Create a new course
Cypress.Commands.add('ustadAddCourse',(courseName) => {
    cy.contains("Courses").click()
    cy.contains("button","Course").click()
    cy.contains("Add a new course").click()
    cy.get('input[id="clazz_name"]').type(courseName)
    cy.get('div[data-placeholder="Description"]').type("test class")
    cy.contains("button","Save").click()
})


// Add a new person
Cypress.Commands.add('ustadAddNewPerson',(firstName,lastName,gender) => {
    cy.contains("span","Add a new person").click()
    cy.contains("label", "First names").parent().find("input").clear().type(firstName)
    cy.contains("label", "Last name").parent().find("input").clear().type(lastName)
    cy.get('div[id="gender"]').click()
    cy.contains("li",gender).click()
    cy.contains("button","Save",{timeout: 2000}).click()
    cy.contains('New enrolment',{timeout: 2000}).should('be.visible')
    cy.contains("button","Save",{timeout: 2000}).click()
})

// Create a user account
Cypress.Commands.add('ustadCreateUserAccount',(userName,password) => {
    cy.contains('Create account').click()
    cy.get('#username:not([disabled])').type(userName)
    cy.get('#newpassword').type(password)
    cy.contains("button","Save").click()
    cy.contains('Change Password',{timeout:2000}).should('be.visible')
    cy.go('back')
    cy.go('back')
})

  // Add a Module Block
Cypress.Commands.add('ustadAddModuleBlock',(moduleTitle) => {
    cy.contains("Add block").click()
    cy.contains("Module").click()
    cy.get('input[id="title"]').type(moduleTitle)
    cy.contains("button","Done").click()
    cy.contains("button","Save").click()
})

  // Add a Text Block
Cypress.Commands.add('ustadAddTextBlock',(textTitle) => {
    cy.contains("Add block").click()
    cy.contains("Text").click()
    cy.get('input[id="title"]').type(textTitle)
    cy.get('div[data-placeholder="Description"]').type("a simple block test")
    cy.contains("button","Done").click()
})


  // Add Assignment block
Cypress.Commands.add('ustadAddAssignmentBlock',(assignmentTitle) => {
    cy.contains("Add block").should('be.visible').click()
    cy.contains("Assignment").click()
    cy.get('input[id="title"]').type(assignmentTitle)
    cy.get('div[data-placeholder="Description"]').type("this is a simple assignment")
    cy.contains("button","Done").click()
})

  // Add a Discussion Board
Cypress.Commands.add('ustadAddDiscussionBoard',(discussionTitle) => {
    cy.contains("Add block").click()
    cy.contains("Discussion board").click()
    cy.get('input[id="title"]').type(discussionTitle)
    cy.get('div[data-placeholder="Description"]').type("a simple discussion description")
    cy.contains("button","Done").click()
})
   // Add comments in assignments
Cypress.Commands.add('ustadAssignmentComments',(commentid,sendid,comment) => {
    cy.get(commentid).click()
    cy.get(commentid).type(comment,{delay:25})
    cy.get(commentid).should('have.value',comment)
    cy.get(sendid).click()
    cy.contains(comment).should('exist')
})

  // Enable User Registration
Cypress.Commands.add('ustadEnableUserRegistration' ,() => {
    cy.get('#settings_button').click()
    cy.contains('Site').click()
    cy.contains('Edit').click()
  //https://docs.cypress.io/api/commands/should#Assert-the-href-attribute-is-equal-to-users
    cy.get('#terms_html_edit .ql-editor').as('editor')
    cy.get('@editor').should('have.attr', 'contenteditable').and('equal', 'true',{timeout:3000})
    cy.get('@editor').click().clear().type("New Terms",{delay:10})
    cy.get('#registration_allowed').click({force:true})
    cy.get('#actionBarButton').should('be.visible')
    cy.get('#actionBarButton').click()
    cy.contains('Yes').should('exist')
})

/*
 * Set a DateTime field: Takes two arguments:
 * Element, Date object (Javascript Date object)
 *
 * e.g.
 * Set a fixed date/time:
 * cy.ustadSetDateTime(cy.get("input#id"), new Date("2017-06-01T08:30"));
 *
 * Set the time to two minutes from now e.g. now plus (2 x 60 x 1000)ms:
 * cy.ustadSetDateTime(cy.get("input#id"), new Date(Date.now() + (2*60*1000))
 */
Cypress.Commands.add("ustadSetDateTime", (element, date) => {
    element.type(date.getFullYear() + "-" + String(date.getMonth()+1).padStart(2, '0') + "-" +
    String(date.getDate()).padStart(2, '0') + "T" + String(date.getHours()).padStart(2, '0') +
    ":" + String(date.getMinutes()).padStart(2,'0')
  );
});


/*
 * e.g.
 * cy.ustadBirthDate(cy.get("input#id"), new Date("2017-06-01"));
 *
*/

Cypress.Commands.add("ustadBirthDate", (element, date) => {
     element.type(date.getFullYear() + "-" + String(date.getMonth()+1).padStart(2, '0') + "-" +
     String(date.getDate()).padStart(2, '0')
     );
});

//commands.js
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })