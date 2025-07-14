describe('WEB_003_003_admin_or_teacher_add_content_via_file', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Admin able to add content block from library', () => {
  cy.importUsersViaHttp("Ustad_Teacher_and_Students.csv")
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass')
  cy.ustadAddContentToLibrary('../test-files/content/Video_Content.mp4','Content_001')
 // Add a new course
  cy.contains("Content_001").should('exist')
  cy.contains('Courses').click({force: 'true'})
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
 // Add content block from file block
  cy.contains("Course").click()
  cy.contains("Test Course Block").click()
  cy.contains("button","Course").click()
  cy.contains("button","Edit").click()
  cy.contains("Add block", { timeout: 5000 }).click()
  cy.get("#add_content_block").click()
  cy.contains('Import from file').click()
  cy.get('input[type="file"]')
    .selectFile('../test-files/content/Epub_Content1.epub',{force: true})
 //Set CourseBlock title
   cy.get('input[id="content_title"]').click()
  cy.get('input[id="content_title"]').clear().type('Content_002',{timeout: 2000})
 //Continue import
  cy.contains('#actionBarButton', 'Next').click()
  cy.contains('#actionBarButton', 'Done').click()
  cy.contains("button","Save").click()
  cy.contains('button','Edit').should('exist')
  cy.contains("Content_001").should('exist')
  cy.contains('Content_002').should('exist')
})

it('Student-1, Attempt - Video content', () => {
  cy.ustadClearDbAndLogin('stud1', 'tests1', { timeout: 8000 })
  cy.contains('Test Course Block').click()
  cy.contains("Content_001").click()
  cy.contains("OPEN").click()

 // Student-1, Attempt-1, video content - 10%
  cy.ustadVerifyVideo();
  cy.get('video')
    .then($video => {
    $video[0].play();
    setTimeout(() => {
     $video[0].pause();
    }, 2000);
    });
// Verify that the video is paused
  cy.get('video', { timeout: 5000 })
    .should('have.prop', 'paused', true);

 // Student-1, Attempt-2, video content - 100%
  cy.contains('Courses').click()
  cy.contains('Test Course Block').click()
  cy.contains("Content_001").click()
  cy.contains("OPEN").click()
  cy.ustadVerifyVideo()
  cy.get('video')
    .then($video => {
      $video[0].play()
    })
 //cy.get('video', { timeout: 15000 }).should('have.attr', 'data-ustad-video-state', 'ended')  // tried but not getting 100% progress
  cy.wait(12000)
})

it('Student-2 user makes attempts on video-2 sec', () => {
  cy.ustadClearDbAndLogin('stud2', 'tests2', { timeout: 8000 })
  cy.contains('Test Course Block').click()
  cy.contains("Content_001").click()
  cy.contains("OPEN").click()
  cy.ustadVerifyVideo();
  cy.get('video')
    .then($video => {
    $video[0].play();
    setTimeout(() => {
     $video[0].pause();
    }, 2000);
    });
// Verify that the video is paused
cy.get('video', { timeout: 5000 })
  .should('have.prop', 'paused', true);
})

it('Student-3 user makes attempts on epub', () => {
  cy.ustadClearDbAndLogin('stud3', 'tests3', { timeout: 8000 })
  cy.contains('Test Course Block').click()
  cy.contains("Content_002").click()
  cy.contains('OPEN').click()
  cy.ustadVerifyEpub('THE ADOPTING OF ROSA MARIE')
  cy.ustadVerifyEpub('The Adopting of Rosa Marie / (A Sequel to Dandelion Cottage)')
  cy.contains("THE ADOPTING OF ROSA MARIE").click()
})

it('Student-1 user able to see attempts made on content 1', () => {
  cy.ustadClearDbAndLogin('stud1', 'tests1', { timeout: 8000 })
  cy.contains('Courses').click()
  cy.contains('Test Course Block').click()
  cy.contains("Content_001").click()
  cy.contains("button", "Attempts", { timeout: 8000 }).click()
  cy.get("#appbar_title").contains("Content_001").should("exist")
  cy.contains("Student 1 : Completed").should('exist')
  cy.get('span[role="progressbar"]').should('exist')
  cy.contains('100%').should('exist')
  cy.contains('Progress').should('exist')
  cy.contains("Student 1 : Completed").click()
  cy.get("#appbar_title").contains("Student 1 - Content_001").should("exist")
  cy.contains("Completed").should("exist")
  cy.get('span[role="progressbar"]').should('exist')
  cy.contains('100%').should('exist')
  cy.contains('Progress').should('exist')
  cy.contains("Completed").click()
  cy.get("#appbar_title").contains("Content_001").should("exist")
  cy.contains('Completed').should('exist')
  cy.contains('100%').should('exist')
  cy.contains('Progress').should('exist')
  cy.get("svg[data-testid='CheckIcon']").should('exist') // Filter already applied by default
  cy.get(".MuiChip-labelMedium").contains("Completed").should("exist") // Filter chip
  cy.get(".MuiListItemText-primary").contains("Completed Content_001").should("exist")
  cy.get(".MuiChip-labelMedium").contains("Progressed").should("exist") // Filter chip
  cy.get(".MuiListItemText-primary").contains("Progressed Content_001").should("exist")
  cy.get('span[role="progressbar"]').should('exist')
  cy.get(".MuiChip-labelMedium").contains("Completed").click() // testing filter chip
  cy.contains("100%").should('not.exist')
  cy.get(".MuiListItemText-primary").contains("Completed Content_001").should("not.exist")
  cy.get(".MuiListItemText-primary").contains("Progressed Content_001").should("exist")
})

it('Student2 user able to see video content attempts made', () => {
  cy.ustadClearDbAndLogin('stud2', 'tests2', { timeout: 8000 })
  cy.contains('Courses').click()
  cy.contains('Test Course Block').click()
  cy.contains("Content_001").click()
  cy.contains("button", "Attempts", { timeout: 8000 }).click()
  cy.get("#appbar_title").contains("Content_001").should("exist")
  cy.contains("Student 2 : Incomplete").should('exist')
  cy.get('span[role="progressbar"]').should('exist')
  cy.contains("Student 2 : Incomplete").click()
  cy.get("#appbar_title").contains("Student 2 - Content_001").should("exist")
  cy.contains('Incomplete').should('exist')
  cy.get('span[role="progressbar"]').should('exist')
  cy.contains('Incomplete').click()
  cy.get("#appbar_title").contains("Content_001").should("exist")
  cy.get(".MuiChip-labelMedium").contains("Progressed").should("exist")
  cy.get(".MuiListItemText-primary").contains("Progressed Content_001").should("exist")
  cy.get('span[role="progressbar"]').should('exist')
})

it('Student3 user able to see epub content attempts made', () => {
  cy.ustadClearDbAndLogin('stud3', 'tests3', { timeout: 8000 })
  cy.contains('Courses').click()
  cy.contains('Test Course Block').click()
  cy.contains('Content_002').click()
  cy.contains("button", "Attempts").click()
  cy.get("#appbar_title").contains("Content_002").should("exist")
  cy.contains("Student 3 : Incomplete").should('exist')
  cy.get('span[role="progressbar"]').should('exist')
  cy.contains("Student 3 : Incomplete").click()
  cy.get("#appbar_title").contains("Student 3 - Content_002").should("exist")
 // Assert attempt score, completion, duration visible
  cy.contains('Incomplete').should('exist')
  cy.get("svg[data-testid='CalendarTodayIcon']").should('exist')
  cy.get('span[role="progressbar"]').should('exist')
  cy.contains('Incomplete').click()
  cy.get(".MuiChip-labelMedium").contains("Progressed").should("exist")
  cy.get(".MuiListItemText-primary").contains("Progressed The Adopting of Rosa Marie / (A Sequel to Dandelion Cottage)").should("exist")
  cy.get("#appbar_title").contains("Content_002").should("exist")
  cy.get('span[role="progressbar"]').should('exist')
})

it('Teacher user can see student users attempts', () => {
  cy.ustadClearDbAndLogin('teach1', 'testt1', { timeout: 8000 })
  cy.contains('Test Course Block').click()
  cy.contains('Content_001').click()
  cy.contains("button", "Attempts").click()
 // verify teacher able to see students list in attempts screen
  cy.contains("Student 1 : Completed").should("exist")
  cy.contains("Student 2 : Incomplete").should("exist")
// *** student 2  **** //
  cy.get("#appbar_title").contains("Content_001").should("exist")
  cy.get('span[role="progressbar"]').should('exist')
  cy.contains("1 Attempts").should('exist')
  cy.contains("Student 2 : Incomplete").click()
  cy.get("#appbar_title").contains("Student 2 - Content_001").should("exist")
  cy.contains('Incomplete').should('exist')
  cy.get('span[role="progressbar"]').should('exist')
  cy.contains('Incomplete').click()
  cy.get("#appbar_title").contains("Content_001").should("exist")
  cy.contains("Progressed Content_001").should("exist")
  cy.get('span[role="progressbar"]').should('exist')
})

it('Student2 cannot see Student1 users attempt', () => {
  cy.ustadClearDbAndLogin('stud2', 'tests2', { timeout: 8000 })
  cy.contains('Test Course Block').click()
  cy.contains("Content_002").click()
  cy.contains("button", "Attempts").click()
 // verify attempts are not visible
  cy.contains('Nothing here, yet').should('exist')
})


  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer()
  })
})