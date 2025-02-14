describe('WEB_001_001_add_content', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Admin able to add content block to course', () => {
  cy.importUsersViaHttp("Ustad_Teacher_and_Students.csv");
 // Admin user login
  cy.ustadClearDbAndLogin('admin', 'testpass');
 // Add content block
  cy.contains("Course").click();
  cy.contains("Test Course Block").click();
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
  cy.get('input[id="title"]').click().clear().type('Content_001', { timeout: 2000 });
  cy.get('input[id="cbMaxPoints"]').click().type("10");
  cy.contains('#actionBarButton', 'Done').click();
  cy.contains("button", "Save", { timeout: 8000 }).click();
  cy.contains('Courses').click();
  cy.contains('Test Course Block').click();
  cy.contains("Content_001").should('exist')
});

it('Student-1 user makes attempts on epub', () => {
  cy.ustadClearDbAndLogin('stud1', 'tests1', { timeout: 8000 });
  cy.contains('Test Course Block').click();
  cy.contains("Content_001").click();
  cy.contains('OPEN').click();
  cy.ustadVerifyEpub('THE ADOPTING OF ROSA MARIE');
  cy.ustadVerifyEpub('The Adopting of Rosa Marie / (A Sequel to Dandelion Cottage)');
  cy.contains("THE PERSONS OF THE STORY").click()
});

it('Student-2 user makes attempts on epub', () => {
  cy.ustadClearDbAndLogin('stud2', 'tests2', { timeout: 8000 });
  cy.contains('Test Course Block').click();
  cy.contains("Content_001").click();
  cy.contains('OPEN').click();
  cy.ustadVerifyEpub('THE ADOPTING OF ROSA MARIE');
  cy.ustadVerifyEpub('The Adopting of Rosa Marie / (A Sequel to Dandelion Cottage)');
  cy.contains('CHAPTER VII Discovery').scrollIntoView();
  cy.contains("CHAPTER VII Discovery").click()
})

it('Student-3 user makes attempts on epub', () => {
  cy.ustadClearDbAndLogin('stud3', 'tests3', { timeout: 8000 });
  cy.contains('Test Course Block').click();
  cy.contains("Content_001").click();
  cy.contains('OPEN').click();
  cy.ustadVerifyEpub('THE ADOPTING OF ROSA MARIE');
  cy.ustadVerifyEpub('The Adopting of Rosa Marie / (A Sequel to Dandelion Cottage)');
  cy.contains('CHAPTER VII Discovery').scrollIntoView();
  cy.contains("CHAPTER VII Discovery").click()
})

it('Teacher generate report', () => {
 // Admin user login
  cy.ustadClearDbAndLogin('teach1','testt1',{timeout:8000})
  cy.contains("Reports").click()
  cy.get("svg[data-testid='AddIcon']").click()
  cy.get("#Title").click().type("Test1")
  cy.get('div[id="x_axis"]').click()
  cy.contains("Day").click()
  cy.get('input[id="Series Title"]').type("Usage time by day this week")
  cy.get('div[id="Y Axis"]').click()
  cy.contains("Total Duration").click()
  cy.get('div[id="Subgroup by"]').click()
  cy.contains("Gender").click()
  cy.get('div[id="Chart Type"]').click()
  cy.contains("Bar Chart").click()
  cy.get('div[id="Time Range"]').click()
  cy.contains("Last week").click()
  cy.contains('button','Done').click()
  cy.contains('button','Edit').click()
  cy.contains('Add filter').scrollIntoView();
  cy.contains('Add filter').click()
  cy.get("div[id='Field']").click()
  cy.contains("Person age").click()
  cy.get('div[id="Condition"]').click()
  cy.contains("Greater").click()
  cy.get('#Value').click().type('13')
  cy.get('#actionBarButton').click()
  cy.get('#actionBarButton').click()
  cy.contains("Reports").click()
  cy.contains("Test1").should("exist")
  cy.get("svg[data-testid='DeleteIcon']").should("exist")
 })

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})