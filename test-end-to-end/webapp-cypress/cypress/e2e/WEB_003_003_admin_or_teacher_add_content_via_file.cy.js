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

it('Student-1, Attempt 1, Video content-for 2 sec', () => {
  cy.ustadClearDbAndLogin('stud1', 'tests1', { timeout: 8000 })
  cy.contains('Test Course Block').click()
  cy.contains("Content_001").click()
  cy.contains("OPEN").click()
  cy.ustadVerifyVideo()
  cy.get('video')
    .then($video => {
      $video[0].play()
    })
  cy.wait(2000) // Wait for 2 seconds to get progress verb for test
})

it('Student-1, Attempt-2, video content - 100%', () => {
  cy.ustadClearDbAndLogin('stud1', 'tests1', { timeout: 8000 })
  cy.contains('Test Course Block').click()
  cy.contains("Content_001").click()
  cy.contains("OPEN").click()
  cy.ustadVerifyVideo()
  cy.get('video')
    .then($video => {
      $video[0].play()
    })
  cy.wait(10500)  // {timeout:15000} didn't work so added wait to make sure the video makes 100% completion
  cy.get('video')
    .should('have.prop', 'ended', true)
})

it('Student-2 user makes attempts on video-2 sec', () => {
  cy.ustadClearDbAndLogin('stud2', 'tests2', { timeout: 8000 })
  cy.contains('Test Course Block').click()
  cy.contains("Content_001").click()
  cy.contains("OPEN").click()
  cy.ustadVerifyVideo()
  cy.get('video')
    .then($video => {
      $video[0].play()
    })
  cy.wait(2000) // Wait for 2 seconds to get progress verb for test
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
  cy.contains("Attempts: 1").should('exist')
  cy.get("#appbar_title").contains("Content_001").should("exist")
  cy.get('.MuiStack-root').parent()
    .find('span[role="progressbar"]').should('exist')
  cy.contains("Student 1").click()
  cy.get("#appbar_title").contains("Student 1 - Content_001").should("exist")
  cy.get('.MuiStack-root').parent()
    .find('span[role="progressbar"]').should('exist')
  cy.contains('Completed').should('exist')
  cy.contains('100% completion').should('exist')
  cy.contains('Completed').click()
  cy.get("#appbar_title").contains("Content_001").should("exist")
  cy.get("svg[data-testid='CheckIcon']").should('exist') // Filter already applied by default
  cy.contains("Completed").should("exist")
    //cy.contains("Completed - title here").should('exist') // title of the question/page
  cy.get('span[role="progressbar"]').eq(0).should('exist')   //--completed
  cy.get('span[role="progressbar"]').eq(1).should('exist') //--progressed
  cy.contains("100% completion").should("exist")
  cy.contains("Completed").click() // testing filter chip
  cy.contains("100% completion").should('not.exist')
  cy.get('span[role="progressbar"]').eq(1).should('not.exist')
  //cy.get("svg[data-testid='CheckIcon']").should('not.exist')
})

it('Student2 user able to see video content attempts made', () => {
  cy.ustadClearDbAndLogin('stud2', 'tests2', { timeout: 8000 })
  cy.contains('Courses').click()
  cy.contains('Test Course Block').click()
  cy.contains("Content_001").click()
  cy.contains("button", "Attempts", { timeout: 8000 }).click()
  cy.get("#appbar_title").contains("Content_001").should("exist")
  cy.contains("Attempts: 1").should('exist')
  cy.get('.MuiStack-root').parent()
      .find('span[role="progressbar"]').should('exist')
  cy.contains("Student 2").click()
  cy.get("#appbar_title").contains("Student 2 - Content_001").should("exist")
  cy.contains('Incomplete').should('exist')
  cy.get('.MuiStack-root').parent()
    .find('span[role="progressbar"]').should('exist')
  cy.contains('Incomplete').click()
  cy.get("#appbar_title").contains("Content_001").should("exist")
  cy.contains("Progressed").should("exist")
  cy.get('.MuiStack-root').parent()
    .find('span[role="progressbar"]').should('exist')
})

it('Student3 user able to see epub content attempts made', () => {
  cy.ustadClearDbAndLogin('stud3', 'tests3', { timeout: 8000 })
  cy.contains('Courses').click()
  cy.contains('Test Course Block').click()
  cy.contains('Content_002').click()
  cy.contains("button", "Attempts").click()
  cy.get("#appbar_title").contains("Content_002").should("exist")
  cy.contains("Attempts: 1").should('exist')
  cy.get('.MuiStack-root').parent()
    .find('span[role="progressbar"]').should('exist')
  cy.contains("Student 3").click()
  cy.get("#appbar_title").contains("Student 3 - Content_002").should("exist")
 // Assert attempt score, completion, duration visible
  cy.contains('Incomplete').should('exist')
  cy.get("svg[data-testid='CalendarTodayIcon']").should('exist')
  cy.get('.MuiStack-root').parent()
    .find('span[role="progressbar"]').should('exist')
  cy.contains('Incomplete').click()
  cy.contains("Progressed").should("exist")
  cy.get("#appbar_title").contains("Content_002").should("exist")
  cy.get('.MuiStack-root').parent()
    .find('span[role="progressbar"]').should('exist')
})

it('Teacher user can see student users attempts', () => {
  cy.ustadClearDbAndLogin('teach1', 'testt1', { timeout: 8000 })
  cy.contains('Test Course Block').click()
  cy.contains('Content_001').click()
  cy.contains("button", "Attempts").click()
 // verify teacher able to see students list in attempts screen
  cy.contains("Student 1").should("exist")
  cy.contains("Student 2").should("exist")
// *** student 2  **** //
  cy.get("#appbar_title").contains("Content_001").should("exist")
  cy.get('span[role="progressbar"]').eq(0).should('exist')
  cy.get('span[role="progressbar"]').eq(1).should('exist')
  cy.contains("Attempts: 1").should('exist')
  cy.contains("Student 2").click()
  cy.get("#appbar_title").contains("Student 2 - Content_001").should("exist")
 // Assert attempt completion, duration visible
  cy.contains("Student 2").click()
  cy.contains('Incomplete').should('exist')
  cy.get('.MuiStack-root').parent()
    .find('span[role="progressbar"]').should('exist')
  cy.contains('Incomplete').click()
  cy.contains("Progressed").should("exist")
  cy.get("#appbar_title").contains("Content_001").should("exist")
  cy.get('.MuiStack-root').parent()
    .find('span[role="progressbar"]').should('exist')
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