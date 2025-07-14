describe('WEB_012_001_user_generate_report', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Admin generate report', () => {
  cy.importUsersViaHttp("Ustad_Teacher_and_Students.csv");
 // Admin user login
  cy.ustadClearDbAndLogin('admin','testpass',{timeout:8000})
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
  cy.UstadContentUsageData('Content_001','stud2')
  cy.contains("Reports").click()
  cy.get("svg[data-testid='AddIcon']").click()
  cy.get("#appbar_title").contains("Add a new report").should("exist")
  cy.get('input[id="title"]').type('E2E')
  cy.contains('div[id="time_range"]','Last week',{timeout:10000}).click()
  cy.contains("Custom date range",{timeout:6000}).click()
 // Set a custom report date range
  const toDate = new Date()
  const fromDate = new Date()
  fromDate.setDate(fromDate.getDate() - 7)
  cy.ustadSetDate(cy.get('input[id="from_date"]'),fromDate,{timeout:12000}) // last week from today
  cy.ustadSetDate(cy.get('input[id="to_date"]'),toDate,{timeout:12000}) // Today
  cy.contains('div[id="x_axis"]',"Day").should('exist')
  cy.get('input[id="series_title"]').click()
  cy.get('input[value="Series 1"]').should('exist')
  cy.contains('div[id="y_axis"]','Total Duration').should('exist')
  cy.get('div[id="subgroup_by"]').click()
  cy.contains("Gender").click()
  cy.contains('div[id="chart_type"]',"Bar Chart").should('exist')
  cy.get('input[id="title"]').type('E2E')
  cy.contains('Add Series',{timeout:6000}).scrollIntoView();
  cy.contains('button','Add Series').click()
  cy.get('input[id="series_title"]').eq(1).click()
  cy.get('input[value="Series 2"]').should('exist')
  cy.get('div[id="y_axis"]').eq(1).click()
  cy.contains('Average Duration').click()
//  cy.contains('Add Filter').scrollIntoView();
//  cy.contains('Add Filter').click()
//  cy.get("#appbar_title").contains("Edit filter").should("exist")
//  cy.get("div[id='field']").click()
//  cy.contains("Person age").click()
//  cy.get('div[id="condition"]').click()
//  cy.contains("> Greater").click()
//  cy.get('#Value').click().type('13')
//  cy.get('#actionBarButton').click()
  cy.get('#actionBarButton').click()
  cy.get("#appbar_title").contains("E2E").should("exist")
  cy.get("#appbar_title").contains("E2E").click()
  cy.wait(1000)  // This wait for the data to get loaded on the graph
// screenshot will be saved as
// cypress/screenshots/spec.cy.js/bar_chart_graph_report
  cy.screenshot('bar_chart_graph_report')
  cy.contains('button','Edit').click()
  cy.get("#appbar_title").contains("Edit report").should("exist")
  cy.get("#appbar_title").contains("Edit report").click()
  cy.contains('div[id="time_range"]','Custom date range',{timeout:10000}).click()
  cy.get('[data-value="CUSTOM_PERIOD"]',{timeout:10000}).click()
  cy.contains('div[id="time_range"]',"Custom period (e.g. last x days/weeks)").should("exist")
  cy.contains("DAY").click()
  cy.contains("WEEK").click()
  cy.get('div[id="chart_type"]').eq(0).click()
  cy.get('[data-value="LINE_GRAPH"]').click()
  cy.get('#actionBarButton').click()
  cy.contains('button','Edit').should("exist")
  cy.get("#appbar_title").contains("E2E").should("exist")
  cy.get("#appbar_title").contains("E2E").click()
  cy.wait(1000)  // This wait for the data to get loaded on the graph
// screenshot will be saved as
// cypress/screenshots/spec.cy.js/Line_chart_graph_report
  cy.screenshot('Line_chart_graph_report')
  cy.contains("Reports").click()
  cy.get("#appbar_title").contains("Reports").should("exist")
  cy.contains("E2E").should("exist")
})

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})