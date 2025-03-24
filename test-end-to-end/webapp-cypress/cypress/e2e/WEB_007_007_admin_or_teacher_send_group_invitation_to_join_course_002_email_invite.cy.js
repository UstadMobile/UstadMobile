describe('WEB_007_007_admin_or_teacher_send_group_invitation_to_join_course_002_email_invite', () => {
 before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Admin enable registration', () => {
    cy.importUsersViaHttp("Ustad_Unenrolled_Teacher_And__Student_list.csv");
  // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
  cy.ustadEnableUserRegistration()
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains("Course").click()
  cy.contains("Members").click()
  cy.contains("Add a student").click()
  cy.contains("Invite Via Contact").click()
  cy.get("input[role='combobox']").click().type("stud1@email.com , stud2@email.com")
  cy.contains("Send").click()
  cy.contains("stud1@email.com").should('exist')
  cy.contains("stud2@email.com").should('exist')
})

it('New student user login via email link', () => {
  cy.ustadClearIndexDb()
   const email = 'stud1@email.com'
   const baseUrl = '/'
// Call the custom command to fetch the email and open the URL
  cy.UstadOpenInviteLinkFromEmail(email, baseUrl, {timeout:60000})
  cy.contains('button[class*="MuiButton-outlinedPrimary"]', 'New user').click();
  cy.ustadBirthDate(cy.get("#age_date_of_birth"), new Date("2010-06-01"));
  cy.contains('button','Next').click()
  cy.contains('New Terms').should('be.visible')
  cy.get('#accept_button').click()
  cy.contains("label", "Full name*").parent().find("input").clear().type('New User')
  cy.get('div[id="gender"]').click()
  cy.contains("li","Female").click()
  cy.contains('button','Next').click()
  cy.contains("label", "Username").parent().find("input").clear().type('newuser')
  cy.contains("label", "Password").parent().find("input").clear().type('test1234')
  cy.contains('SIGN-UP').click()
  cy.contains("Do you want to join this course?").should("exist")
  cy.contains("ACCEPT").click()
  cy.contains("Test Course Block",{timeout: 10000}).should("exist")
})

it('Tearcher verify student added to the course', () => {
 // Teacher Login
  cy.ustadClearDbAndLogin('teach1','testt1')
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains("Course").click()
  cy.contains("Members").click()
  cy.contains("New User").should('be.visible')
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})