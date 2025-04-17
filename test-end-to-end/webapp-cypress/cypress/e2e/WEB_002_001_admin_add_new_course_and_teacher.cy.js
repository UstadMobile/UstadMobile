describe('WEB_002_001_admin_add_new_course_and_teacher ', () => {
  before(() => {
     // Start Test Server
     cy.ustadStartTestServer(6000)
  })

it('Admin user create a course and add members to the course', () => {
// Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
// verify course title is mandatory
  cy.contains("Courses").click()
  cy.contains("button","Course").click()
  cy.contains("Add a new course").click()
  cy.contains("button","Save").click()
  cy.get('.MuiInputBase-colorPrimary.Mui-error').should('exist')
  cy.get('input[id="clazz_name"]').type('Test Course Block')
// Adding course banner pic
  cy.get('svg[data-testid="AddAPhotoIcon"]').click()
  cy.get('input[type="file"]').selectFile('../test-files/content/courseBannerPic.jpg',{force:true})
  cy.contains("button","Save").click()
// Add a teacher
  cy.contains("button","Members").click()
  cy.contains("span","Add a teacher").click()
  cy.ustadAddNewPerson('Teacher','1','Female')
 //Add a student
  cy.contains("span","Add a student").click()
  cy.ustadAddNewPerson('Student','1','Male')
  cy.contains("button","Members").should('be.visible')
//Add account for student
  cy.contains("Student 1").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('stud1','tests1')
  cy.go('back')
  cy.go('back')
// Add account for teacher
  cy.contains("Teacher 1").click()
  cy.contains('View profile').click()
  cy.ustadCreateUserAccount('teach1','testt1')
  cy.contains('Permissions').click()
  cy.contains('button', 'Edit').click()
  cy.wait(1000)
  //cy.get(".MuiFormControlLabel-root.Mui-disabled",{timeout:6000}).should('not.exist')
  cy.contains('Add new courses',{timeout:6000}).should('be.visible')
  cy.contains('Add new courses',{timeout:5000}).click()
  cy.contains("button","Save",{timeout:5000}).click()
// Add course blocks
  cy.contains("Courses").click()
  cy.contains('Test Course Block').click()
// Add module block
  cy.contains('button', 'Edit').click();
  cy.contains('Add block').click();
  cy.contains('Module').click();
  cy.get('input[id="title"]').type('Term 1');
  cy.contains('button', 'Done').click();
  cy.contains('Term 1').should('be.visible')
  cy.contains('button', 'Save').click();
// Add Assignment
  cy.contains('button', 'Edit').click();
  cy.contains('Add block').should('be.visible').click();
  cy.contains('Assignment').click();
  cy.get('input[id="title"]').type('Assignment 1')
  cy.ustadSetDateTime(cy.get("#cbDeadlineDate"), new Date(Date.now() - (365 * 24 * 60 * 60 * 1000))) // last year
  cy.contains('button', 'Done').click()
  cy.contains('Assignment 1').should('be.visible')
// Set Course start and end dates
  cy.ustadSetDate(cy.get("#clazz_start_time"), new Date(Date.now() - (2 * 365 * 24 * 60 * 60 * 1000))) // 2 years ago
  cy.ustadSetDate(cy.get("#clazz_end_time"), new Date(Date.now() - (365 * 24 * 60 * 60 * 1000))) // last year
// Add Discussion board
  cy.ustadAddDiscussionBoard('Discussion 1')
  cy.contains('Edit course').should('be.visible')
  cy.contains("button","Save",{timeout:5000}).click()
// Add post to the discussion

  cy.contains("Courses").click()
  cy.contains('All').click()
  cy.contains('Test Course Block').click()
  cy.contains('.MuiTypography-root','Discussion 1').click()
  cy.contains('Post').click()
  cy.get('#discussion_post_title').type('Post 1')
  cy.get('.ql-editor').ustadTypeAndVerify('Discusssion post')
  cy.get('#actionBarButton').click()
  cy.contains('Post 1',{timeout:5000}).should('be.visible')
// Copy the existing course
  cy.contains("Courses").click()
  cy.contains('All').click()
  cy.contains('Test Course Block').click()
  cy.contains('Copy').should('be.visible')
  cy.contains('Copy').click()
  cy.contains("#appbar_title", "Copy course").should("be.visible")
  cy.get("input[value='Copy of Test Course Block']").should("be.visible")
  cy.contains('Term 1').should('exist')
  cy.contains('Assignment 1').should('exist')
  cy.scrollTo('bottom')
  cy.contains('Assignment 1').click()
  cy.ustadSetDateTime(cy.get("#cbDeadlineDate"), new Date(Date.now() + (24 * 60 * 60 * 1000))) // tomorrow
  cy.get('#caSubmissionPolicy').click()
  cy.contains('Can make multiple submissions').click()
  cy.contains("button","Done").should('be.visible')
  cy.contains("button","Done").click()
  cy.ustadSetDate(cy.get("#clazz_start_time"), new Date(Date.now() - (24 * 60 * 60 * 1000))) //yesterday
  cy.get("#clazz_end_time").clear()
  cy.contains('Add block').click();
  cy.contains('Text').click();
  cy.get('input[id="title"]').type('Text 1');
  cy.contains('button', 'Done').click()
  cy.contains('Text 1').should('exist')
  cy.contains('button', 'Save',{timeout:5000}).click()
  cy.contains('button', 'Save',{timeout:5000}).should('not.exist')
  cy.contains('Term 1').should('exist')
  cy.contains('Text 1').should('exist')
  cy.contains('Assignment 1').should('exist')
  cy.contains('Discussion 1').click()
  cy.contains('Post 1').should('not.exist')
  cy.contains('Courses').click()
  cy.contains('Copy of Test Course Block').should('exist')
})

it('Teacher has permission to add a course ', () => {
  cy.ustadClearDbAndLogin('teach1','testt1')
  cy.contains('All').click()
  cy.contains('Test Course Block').should('exist')
  cy.contains('Test Course Block').click()
  cy.contains('Copy').should('exist')
  cy.contains('Copy').click()
  cy.contains("#appbar_title", "Copy course").should("be.visible")
  cy.get("input[value='Copy of Test Course Block']").should("be.visible")
})

it('Student has no permission to add a course ', () => {
  cy.ustadClearDbAndLogin('stud1','tests1')
  cy.contains('All').click()
  cy.contains('Test Course Block').should('exist')
  cy.contains('Test Course Block').click()
  cy.contains('Copy').should('not.exist')
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})