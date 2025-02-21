describe('WEB_003_005_admin_or_teacher_and_content_via_file', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Admin able to add content block from library', () => {
  cy.importUsersViaHttp("Ustad_Teacher_and_Students.csv");
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
  cy.ustadAddContentToLibrary('../test-files/content/Video_Content.mp4','Content_001')
 // Add a new course
  cy.contains("Content_001").should('exist')
  cy.contains('Courses').click({force: 'true'});
  cy.contains('Test Course Block').click()
  cy.contains('button','Edit').click()
  cy.contains("Add block").click()
  cy.contains("Content").click()
  cy.contains("Content_001").click()
  cy.contains("Done").click()
  cy.contains("Save").click()
  cy.contains('button','Edit').should('exist')
  cy.contains("Content_001").should('exist')
})

it('Teacher able to add content block from file', () => {
  cy.ustadClearDbAndLogin('teach1','testt1')
 // Add Assignment block
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains("button","Course").click()
  cy.contains("button","Edit").click()
  cy.contains("Add block").click()
  cy.get("#add_content_block").click()
  cy.contains('Import from file').click()
  cy.get('input[type="file"]')
    .selectFile('../test-files/content/Epub_Content1.epub',{force: true})

  //Continue import
  cy.contains('#actionBarButton', 'Next').click()

  //Set CourseBlock title
  cy.contains("#appbar_title", "Edit content block").should("be.visible")
  cy.get('input[id="title"]').click()
  cy.get('input[id="title"]').clear().type('Content_002',{timeout: 2000})
  cy.contains('#actionBarButton', 'Done').click()
  cy.contains("button","Save").click()
  cy.contains('button','Edit').should('exist')
  cy.contains("Content_001").should('exist')
  cy.contains('Content_002').should('exist')

})

it('Student-1, Attempt 1, Video content-10%', () => {
  cy.ustadClearDbAndLogin('stud1', 'tests1', { timeout: 8000 });
  cy.contains('Test Course Block').click();
  cy.contains("Content_001").click();
  cy.contains("OPEN").click();
  cy.ustadVerifyVideo()
  cy.get('video')
    .then($video => {
      $video[0].play();
    });

  cy.wait(2000); // Wait for 5 seconds
})

it('Student-1, Attempt-2, video content', () => {
  cy.ustadClearDbAndLogin('stud1', 'tests1', { timeout: 8000 });
  cy.contains('Test Course Block').click();
  cy.contains("Content_001").click();
  cy.contains("OPEN").click();
  cy.ustadVerifyVideo()
  cy.get('video')
    .then($video => {
      $video[0].play();
    });

  cy.wait(11000); // Wait for 11 seconds
  cy.get('video')
    .should('have.prop', 'ended', true);
})

it('Student-2 user makes attempts on video', () => {
  cy.ustadClearDbAndLogin('stud2', 'tests2', { timeout: 8000 });
  cy.contains('Test Course Block').click();
  cy.contains("Content_001").click();
  cy.contains("OPEN").click();
  cy.ustadVerifyVideo()
  cy.get('video')
    .then($video => {
      $video[0].play();
    });
  cy.wait(2000); // Wait for 2 seconds
});

it('Student-3 user makes attempts on epub', () => {
  cy.ustadClearDbAndLogin('stud3', 'tests3', { timeout: 8000 });
  cy.contains('Test Course Block').click();
  cy.contains("Content_002").click();
  cy.contains('OPEN').click();
  cy.ustadVerifyEpub('THE ADOPTING OF ROSA MARIE');
  cy.ustadVerifyEpub('The Adopting of Rosa Marie / (A Sequel to Dandelion Cottage)');
  cy.contains("THE ADOPTING OF ROSA MARIE").click()
})

it('Student-1 user able to see attempts made on content 1', () => {
  cy.ustadClearDbAndLogin('stud1', 'tests1', { timeout: 8000 });
  cy.contains('Courses').click();
  cy.contains('Test Course Block').click();
  cy.contains("Content_001").click();
  cy.contains("button", "Attempts", { timeout: 8000 }).click();
  cy.contains("Attempts: 1").should('exist');
  cy.get('.MuiStack-root')
    .parent() // Get the parent container
    .find('span[role="progressbar"]')
    .should('exist');
  cy.contains('100% Completion').should('exist');
 // Assert progress bar visible
  cy.contains("Student 1").click();
  cy.contains('Completed').should('exist');
  cy.contains('100% Completion').should('exist');
  cy.contains('Completed').click()
  cy.contains('Completion: 100%').should('exist');
  cy.get('.MuiStack-root')
    .parent() // Get the parent container
    .find('span[role="progressbar"]')
    .should('exist');
})

it('Student2 user able to see epub content attempts made', () => {
  cy.ustadClearDbAndLogin('stud2', 'tests2', { timeout: 8000 });
  cy.contains('Courses').click();
  cy.contains('Test Course Block').click();
  cy.contains("Content_001").click();
  cy.contains("button", "Attempts", { timeout: 8000 }).click();
  cy.contains("Attempts: 1").should('exist');
  cy.get('.MuiStack-root')
    .parent() // Get the parent container
    .find('span[role="progressbar"]')
    .should('exist');
 // Assert progress bar visible
  cy.contains("Student 2").click();
  cy.contains('Incomplete').should('exist');
  cy.get('.MuiStack-root')
      .parent() // Get the parent container
      .find('span[role="progressbar"]')
      .should('exist');
  cy.contains('Incomplete').click()
  cy.get('.MuiStack-root')
      .parent() // Get the parent container
      .find('span[role="progressbar"]')
      .should('exist');

})

it('Student3 user able to see epub content attempts made', () => {
  cy.ustadClearDbAndLogin('stud3', 'tests3', { timeout: 8000 });
  cy.contains('Courses').click();
  cy.contains('Test Course Block').click();
  cy.contains('Content_002').click();
  cy.contains("button", "Attempts").click();
 // Assert progress bar visible
  cy.contains("Attempts: 1").should('exist');
  cy.get('.MuiStack-root')
      .parent() // Get the parent container
      .find('span[role="progressbar"]')
      .should('exist');
  cy.contains("Student 3").click();
 // Assert attempt score, completion, duration visible
  cy.contains('Incomplete').should('exist');
  cy.get('svg[data-testid="TimerIcon"]').should('exist');
  cy.get('.MuiStack-root')
      .parent() // Get the parent container
      .find('span[role="progressbar"]')
      .should('exist');
  cy.contains('Incomplete').click();
  cy.contains("[data-testid='CloseIcon']").should('not.exist'); // wrong icon
  cy.get('.MuiStack-root')
      .parent() // Get the parent container
      .find('span[role="progressbar"]')
      .should('exist');
});

it('Teacher user can see student users attempts', () => {
  cy.ustadClearDbAndLogin('teach1', 'testt1', { timeout: 8000 });
// *** student 1  **** //
  cy.contains('Test Course Block').click();
  cy.contains('Content_001').click();
  cy.contains("button", "Attempts").click();
 // Assert progress bar visible
  cy.contains('100% Completion').should('exist');
  cy.contains("Attempts: 1").should('exist');
  cy.contains("Student 1").click();
 // Assert attempt completion, duration visible
  cy.contains('Completed').should('exist');
  cy.contains('100% Completion').should('exist');
  cy.contains('Completed').click()
  cy.contains('Completion: 100%').should('exist');

  cy.get('.MuiStack-root')
      .parent() // Get the parent container
      .find('span[role="progressbar"]')
      .should('exist');
 // verify filter - completed filter chip is tested
  cy.get('#completed_button').click()
  cy.contains('Completion: 100%').should('exist');
  cy.get('.MuiStack-root')
      .parent() // Get the parent container
      .find('span[role="progressbar"]')
      .should('exist');

// *** student 2  **** //
  cy.contains('Courses').click()
  cy.contains('Test Course Block').click();
  cy.contains('Content_001').click();
  cy.contains("button", "Attempts").click();
 // Assert progress bar visible
  cy.get('.MuiStack-root')
      .parent() // Get the parent container
      .find('span[role="progressbar"]')
      .should('exist');
  cy.contains("Attempts: 1").should('exist');
  cy.contains("Student 2").click();
 // Assert attempt completion, duration visible
  cy.contains("Student 2").click();
  cy.contains('Incomplete').should('exist');
  cy.get('.MuiStack-root')
      .parent() // Get the parent container
      .find('span[role="progressbar"]')
      .should('exist');
  cy.contains('Incomplete').click()
  cy.get('.MuiStack-root')
      .parent() // Get the parent container
      .find('span[role="progressbar"]')
      .should('exist');
// *** student 3  **** //
  cy.contains('Courses').click()
  cy.contains('Test Course Block').click();
  cy.contains('Content_002').click();
  cy.contains("button", "Attempts").click();
 // Assert progress bar visible
  cy.contains("Student 3").should('exist');
  cy.get('.MuiStack-root')
      .parent() // Get the parent container
      .find('span[role="progressbar"]')
      .should('exist');
  cy.contains("Attempts: 1").should('exist');
  cy.contains("Student 3").click();
 // Assert attempt completion, duration visible
  cy.contains('Incomplete').should('exist');
  cy.get('svg[data-testid="TimerIcon"]').should('exist');
  cy.get('.MuiStack-root')
      .parent() // Get the parent container
      .find('span[role="progressbar"]')
      .should('exist');
  cy.contains('Incomplete').click();
  cy.get('.MuiStack-root')
      .parent() // Get the parent container
      .find('span[role="progressbar"]')
      .should('exist');
})

it('Student2 cannot see Student1 users attempt', () => {
  cy.ustadClearDbAndLogin('stud2', 'tests2', { timeout: 8000 });
  cy.contains('Test Course Block').click();
  cy.contains("Content_002").click();
  cy.contains("button", "Attempts").click();
 // Assert attempts are not visible
  cy.contains('Nothing here, yet').should('exist');
})


  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})