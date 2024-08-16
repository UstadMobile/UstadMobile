
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


/*
 * Error: ResizeObserver loop limit exceeded is a benign error - it just means that things
 * were moving quickly and a resize update for a frame might have been missed.
 *
 * See: https://github.com/cypress-io/cypress/issues/8418
 */
Cypress.on('uncaught:exception', (err) => {
  if (err.message.includes('ResizeObserver')) {
    // returning false here prevents Cypress from
    // failing the test
    return false;
  }
  return true;
});

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
  cy.get("#sidebar_ContentEntryListHome").click()
  cy.contains("button","Content").click()
  cy.get('#new_content_from_file').click({force: true})
  cy.get('input[type="file"]').selectFile(contentPath,{force:true})
  cy.get('input[id="content_title"]').click()
  cy.get('input[id="content_title"]').clear().type(contentName,{timeout: 2000})
  cy.get('#actionBarButton').click()
})

// Add a Folder to Library
Cypress.Commands.add('ustadAddFolderToLibrary',(folderName) => {
  cy.contains("button","Content").click()
  cy.contains('New folder').click({force: true})
  cy.get('input[id="content_title"]').type(folderName)
  cy.get('#actionBarButton').click()
})

/***
  Open H5p/Epub content
  Cypress doesn't work with popups/new windows,
   so take the URL in cypress, then add "&target=_top" to the url, and navigate to that url.
   Once you are on that URL, then click the open button. It will then open content in the same window
***/

Cypress.Commands.add('ustadOpenH5pEpub', (ContentName) => {
  cy.url().then((url) => {
 // Modify the URL by appending the target parameter
  const modifiedUrl = url + '&target=_top';
 // Visit the modified URL
  cy.visit(modifiedUrl)
  cy.contains("#courseblock_title", ContentName).should("be.visible")
  cy.contains("importing").should("not.exist")
  cy.contains('OPEN').click()
})
})

/* Open an H5P:
 * a) Add the _top parameter to the URL to prevent the default behavior of opening in a new window
 *    (as cypress does not support new windows)
 * b) Use cypress-recurse to retry clicking the open button if needed, checking for the h5p-container
 *    element (see inline comment explaining why this is needed)
 */
Cypress.Commands.add('ustadOpenH5P', (ContentName) => {
    cy.ustadAddTargetToUrl("_top")

    /* For no apparent reason, the open button has been flakey on opening an h5p in automated tests.
     * Logging did not catch even the click event itself. No such issue has ever been seen outside
     * automated testing. Therefor we are using cypress-recurse here to try clicking open again if
     * needed.
     */
    cy.recurse(
        () => {
            return cy.get("body").then(($body) => {
                if($body.find("#open_button").length > 0) {
                    $body.find("#open_button").click()
                }
            })
        },
        ($el) => $el.find("#h5p-container").length > 0
    )
})

Cypress.Commands.add('ustadAddTargetToUrl', (target) => {
    cy.url().then((url) => {
        const modifiedUrl = url + '&target=' + target;
        cy.visit(modifiedUrl)
    })
})

/*****
    Verify H5p Content
    -------------------
    https://www.lambdatest.com/blog/how-to-handle-iframes-in-cypress/
    The iframe reference is from the above link
*****/
Cypress.Commands.add('ustadGetH5pBody', () => {
  cy.get('.h5p-iframe-wrapper')
    .find('.h5p-iframe.h5p-initialized')
    .its('0.contentDocument')
    .its('body')
})

// Verify Epub content
Cypress.Commands.add('ustadVerifyEpub', (epubText) => {
 cy.contains(epubText).should('be.visible')
})

// Verify video content (duration > 0)
Cypress.Commands.add('ustadVerifyVideo', () => {
   cy.get('video').should(($video) => {
    expect($video[0].duration).to.be.gt(0)
  })
})

// Create a new course
Cypress.Commands.add('ustadAddCourse',(courseName) => {
    cy.contains("Courses").click()
    cy.contains("button","Course").click()
    cy.contains("Add a new course").click()
    cy.get('input[id="clazz_name"]').type(courseName)
    cy.get('div[data-placeholder="Description"][contenteditable="true"]').type("test class")
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

// Type and verify text - for use with the rich text editor that can be flakey in tests
Cypress.Commands.add('ustadTypeAndVerify', { prevSubject: 'element' }, (subject, expectedText, options = {}) => {
  // Log statement at the beginning of the command function
  cy.log("Starting ustadTypeAndVerify command..");

  return cy.recurse(
    () => {
      cy.log("Inside recurse command 1");
      return cy.wrap(subject).clear().type(expectedText, { delay: 30 });
    },
    ($el) => {
      cy.log("Inside recurse command 2", Cypress.$($el).text());
      const text = Cypress.$($el).text(); // Accessing the text content using jQuery's text() function
      return text === expectedText;
    },
    {
      limit: options.maxRetries || 3, // Maximum number of retries
    }
  );
});

/*
 * The student list in assignment very rarely does not load as expected. This has never been seen
 * outside of the automated test environment.
 *
 * See https://github.com/bahmutov/cypress-recurse/blob/main/cypress/e2e/reload-page/reload-spec.js
 */
Cypress.Commands.add("ustadReloadUntilVisible", (text) => {
    cy.recurse(
        () => {
            cy.wait(1000)
            return cy.contains(text).should(Cypress._.noop)
        },
        ($el) => $el && $el.text().includes(text),
        {
            limit: 10,
            delay: 1000,
            timeout: 10000,
            post: () => {
                cy.reload()
            }
        }
    )
})


//Scroll until a subject is visible
Cypress.Commands.add('ustadScrollUntilVisible', { prevSubject: 'element' }, (subject, options = {}) => {

  // Set scrollElement to options.scrollElement if provided, otherwise default to "#VirtualList"
  let scrollElement = options.scrollElement || "#VirtualList"
  // Set retryLimit to options.retryLimit if provided, otherwise default to 3
  let retryLimit = options.retryLimit || 3
  let retries = 0;

  const scrollAndVerify = () => {
    if (Cypress.dom.isVisible(subject)) {
      cy.wrap(subject).should('exist')
    } else {
      // scroll to bottom
      cy.get(scrollElement).scrollTo('bottom')
      retries++
      // Retry if the maximum number of retries is not reached
      if (!Cypress.dom.isVisible(subject) && retries <= retryLimit) {
        scrollAndVerify()
      } else {
        // Log an error if the maximum number of retries is reached
        cy.log("Maximum retries reached.")
      }
    }
  }

  // Start the function to scroll and verify
  scrollAndVerify()
})


   // Add course and private comments in Assignment
Cypress.Commands.add('ustadTypeAndSubmitAssignmentComment', (commentid, sendid, comment, delay = 25) => {
    cy.get(commentid).click().type(comment, { delay })
    cy.get('input' + commentid + '[value=\"' + comment + '\"]')
    cy.get(commentid).should('have.value', comment)
    cy.get(sendid).click()
    cy.contains('.MuiListItemText-secondary',comment).should('exist')
})


  // Enable User Registration
Cypress.Commands.add('ustadEnableUserRegistration' ,() => {
    cy.get('#settings_button').click()
    cy.contains('Site').click()
    cy.contains('Edit').click()
  //https://docs.cypress.io/api/commands/should#Assert-the-href-attribute-is-equal-to-users
    cy.get('#terms_html_edit .ql-editor').as('editor')
    cy.get('@editor').should('have.attr', 'contenteditable').and('equal', 'true',{timeout:3000})
    cy.get('@editor').click().clear().ustadTypeAndVerify("New Terms")
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
