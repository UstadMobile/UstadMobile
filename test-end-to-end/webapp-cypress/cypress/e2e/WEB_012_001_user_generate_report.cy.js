describe('WEB_012_001_user_generate_report', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Teacher generate report', () => {
  cy.importUsersViaHttp("Ustad_Teacher_and_Students.csv");
 // Admin user login
  cy.ustadClearDbAndLogin('teach1','testt1',{timeout:8000})

  cy.contains("Course").click()
    cy.contains("Test Course Block").click()
    cy.contains("button","Course").click()
    cy.contains("button","Edit").click()
    cy.contains("Add block").click()
    cy.get("#add_content_block").click()
    cy.contains('Import from file').click()
    cy.get('input[type="file"]')
      .selectFile('../test-files/content/Epub_Content1.epub',{force: true})
   //Set CourseBlock title
     cy.get('input[id="content_title"]').click()
    cy.get('input[id="content_title"]').clear().type('Content_001',{timeout: 2000})
   //Continue import
    cy.contains('#actionBarButton', 'Next').click()
    cy.contains('#actionBarButton', 'Done').click()
    cy.contains("button","Save").click()
    cy.contains('button','Edit').should('exist')
    cy.contains('Content_001').should('exist')
cy.request('GET', '/api/generate-xapi-statements/runtest?contentTitle=Content_001&username=stud1')
.then((response) => {
     expect(response.status).to.eq(200);
     expect(response.body.message).to.eq("Successfully generated test statements");
   })
  //cy.UstadContentUsageData('Content_001','stud1')

  cy.contains("Reports").click()
  cy.get("svg[data-testid='AddIcon']").click()
  cy.get('input[id="title"]').clear().type('R1',{delay: 30})
  cy.get('div[id="time_range"]').click()
 // Set a custom report date range
  const toDate = new Date()
  const fromDate = new Date()
  fromDate.setDate(fromDate.getDate() - 7)
  cy.contains("Custom date range").click()
  cy.ustadSetDate(cy.get('input[id="from_date"]'),fromDate) // last week from today
  cy.ustadSetDate(cy.get('input[id="to_date"]'),toDate) // Today
  cy.get('div[id="x_axis"]').click()
  cy.contains("Day").click()
  cy.get('input[id="series_title"]').type("Series 1")
  cy.get('div[id="y_axis"]').click()
  cy.contains("Total Duration").click()
  cy.get('div[id="subgroup_by"]').click()
  cy.contains("Gender").click()
  cy.get('div[id="chart_type"]').click()
  cy.contains("Bar Chart").click()
  cy.get('input[id="title"]').clear().type('R1',{delay: 30})
  cy.contains('button','Done').click()
  cy.contains('button','Edit').click()
  cy.contains('Add filter').scrollIntoView();
  cy.contains('Add filter').click()
  cy.get("div[id='field']").click()
  cy.contains("Person age").click()
  cy.get('div[id="condition"]').click()
  cy.contains("Greater").click()
  cy.get('#Value').click().type('13')
  cy.get('#actionBarButton').click()
  cy.get('#actionBarButton').click()
// screenshot will be saved as
// cypress/screenshots/spec.cy.js/bar_chart_graph_report
  cy.screenshot('bar_chart_graph_report')
  cy.contains('button','Edit').click()
  cy.get('div[id="time_range"]').click()
  cy.contains("Custom period (e.g. last x days/weeks)").click()
  cy.get('div[id="chart_type"]').click()
  cy.contains("Line Chart").click()
  cy.get('#actionBarButton').click()
  cy.screenshot('Line_chart_graph_report')
  cy.contains("Reports").click()
  cy.contains("R1").should("exist")
  cy.get("svg[data-testid='DeleteIcon']").should("exist")
 })

  /*after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })*/
})