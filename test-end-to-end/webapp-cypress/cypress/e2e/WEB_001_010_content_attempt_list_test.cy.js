describe('WEB_001_010_content_attempt_list_test', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000);
  });

  it('Admin able to add content block from library', () => {
    // Admin user login
    cy.ustadClearDbAndLogin('admin', 'testpass');
    cy.ustadAddContentToLibrary('../test-files/content/H5p_Content.h5p', 'Content_001');

    // Add a new course
    cy.contains("Courses").click();
    cy.ustadAddCourse('Test Course');

    // Add a teacher
    cy.contains("button", "Members").click();
    cy.contains("span", "Add a teacher").click();
    cy.ustadAddNewPerson('Teacher', '1', 'Female');

    // Add account for teacher
    cy.contains("Teacher 1").click();
    cy.contains('View profile').click();
    cy.ustadCreateUserAccount('teach1', 'testt1');

    // Add a student1
    cy.contains("span", "Add a student").click();
    cy.ustadAddNewPerson('Student', '1', 'Male');
    cy.contains("button", "Members").should('be.visible');

    // Add account for student1
    cy.contains("Student 1").click();
    cy.contains('View profile').click();
    cy.ustadCreateUserAccount('stud1', 'tests1');

    // Add a student2
    cy.contains("span", "Add a student").click();
    cy.ustadAddNewPerson('Student', '2', 'Male');
    cy.contains("button", "Members").should('be.visible');

    // Add account for student2
    cy.contains("Student 2").click();
    cy.contains('View profile').click();
    cy.ustadCreateUserAccount('stud2', 'tests2');

    // Add Content from library
    cy.contains('button', 'Course').click();
    cy.contains('button', 'Edit').click();
    cy.contains("Add block").click();
    cy.contains("Content").click();
    cy.get('#content_entry_filter_chip_box').contains('Library').click();
    cy.contains("Content_001").click();
    cy.get('input[id="cbMaxPoints"]').click().type("10");
    cy.contains("Done").click();
    cy.contains("Save").click();
    cy.contains('button', 'Edit').should('exist');

    // Add content block
    cy.contains("Course").click();
    cy.contains("Test Course").click();
    cy.contains("button", "Course").click();
    cy.contains("button", "Edit").click();
    cy.contains("Add block").click();
    cy.get("#add_content_block").click();
    cy.contains('Import from file').click();
    cy.get('input[type="file"]').selectFile('../test-files/content/Epub_Content1.epub', { force: true });

    // Continue import
    cy.contains('#actionBarButton', 'Next').click();

    // Set CourseBlock title
    cy.contains("#appbar_title", "Edit content block").should("be.visible");
    cy.get('input[id="title"]').click().clear().type('Content_002', { timeout: 2000 });
    cy.get('input[id="cbMaxPoints"]').click().type("10");
    cy.contains('#actionBarButton', 'Done').click();
    cy.contains("button", "Save").click();
    cy.contains('button', 'Edit').should('exist');

    // Verify H5P content
    cy.contains("Content_001").click();
    cy.ustadOpenH5P('Content_001');
    cy.ustadGetH5pBody().find(".h5p-question-check-answer.h5p-joubelui-button", "Check").should("be.visible");
    cy.go('back').go('back').go('back');

    // Verify Epub content
    cy.contains('Content_002').click();
    cy.ustadOpenH5pEpub('The Adopting of Rosa Marie');
    cy.ustadVerifyEpub('THE ADOPTING OF ROSA MARIE');
  });

  it('Student1 user makes attempts on H5p', () => {
    cy.ustadClearDbAndLogin('stud1', 'tests1', { timeout: 8000 });
    cy.contains('Test Course').click();
    cy.contains("Content_001").click();
    cy.contains("Importing", { timeout: 20000 }).should("not.exist"); // In case importing
    cy.ustadOpenH5P("Content_001");
    cy.ustadGetH5pBody().find(".h5p-question-check-answer.h5p-joubelui-button", "Check").should("be.visible");
    cy.ustadGetH5pBody().find(".h5p-true-false-answer", "Yes").first().click();
    cy.wait(2000);
  });

  it('Student1 user makes attempts on epub', () => {
    cy.ustadClearDbAndLogin('stud1', 'tests1', { timeout: 8000 });
    cy.contains('Test Course').click();
    cy.contains("Content_002").click();
    cy.contains('OPEN').click();
    cy.ustadVerifyEpub('THE ADOPTING OF ROSA MARIE');
    cy.ustadVerifyEpub('The Adopting of Rosa Marie / (A Sequel to Dandelion Cottage)');
  });

  it('Student1 user able to see attempts made', () => {
    cy.ustadClearDbAndLogin('stud1', 'tests1', { timeout: 8000 });
    cy.contains('Courses').click();
    cy.contains('Test Course').click();
    cy.contains("Content_001").click();
    cy.contains("button", "Attempts", { timeout: 8000 }).click();
    cy.contains("Student 1").should('exist');
    cy.contains("1 Attempts").should('exist');

    // Assert progress bar visible
    cy.contains("Student 1").click();
    cy.contains('Incomplete').should('exist');
    cy.contains('0% Completion').should('exist');

    cy.contains('Courses').click();
    cy.contains('Test Course').click();
    cy.contains('Content_002').click();
    cy.contains('OPEN').click();
    cy.contains("button", "Attempts").click();

    // Assert progress bar visible
    cy.contains('0% Completion').should('exist');
    cy.contains("Student 1").click();

    // Assert attempt score, completion, duration visible
    cy.contains('Incomplete').should('exist');
    cy.contains('0% Completion').should('exist');
  });

  it('Teacher user can see student users attempts', () => {
    cy.ustadClearDbAndLogin('teach1', 'testt1', { timeout: 8000 });
    cy.contains('Test Course').click();
    cy.contains("Content_001").click();
    cy.contains("button", "Attempts").click();

    // Assert progress bar visible
    cy.contains('0% Completion').should('exist');
    cy.contains("Student 1").click();

    // Assert attempt score, completion, duration visible
    cy.contains('Incomplete').should('exist');
    cy.contains('0% Completion').should('exist');
  });

  it('Student2 cannot see Student1 users attempt', () => {
    cy.ustadClearDbAndLogin('stud2', 'tests2', { timeout: 8000 });
    cy.contains('Test Course').click();
    cy.contains("Content_001").click();
    cy.contains("button", "Attempts").click();
    // Assert attempts are not visible
    cy.contains('Nothing here, yet').should('exist');
  });

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  });
});
