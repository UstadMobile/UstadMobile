describe('WEB_012_001_user_generate_report', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000)
  })

it('Teacher generate report', () => {
  cy.importUsersViaHttp("Ustad_Teacher_and_Students.csv");
 // Admin user login
  cy.ustadClearDbAndLogin('teach1','testt1',{timeout:8000})
  cy.contains("Reports").click()
  cy.get("svg[data-testid='AddIcon']").click()
  cy.get('input[id="title"]').clear().type('Test1',{delay: 30})
  cy.get('div[id="time_range"]').click()
 // Set a custom report date range
  const toDate = new Date();
  const fromDate = new Date();
  fromDate.setMonth(fromDate.getMonth() - 1);
  cy.contains("Custom date range").click()
  cy.ustadSetDate(cy.get('input[id="from_date"]'),fromDate) // 1 month from today
  cy.ustadSetDate(cy.get('input[id="to_date"]'),toDate) // Today
  cy.get('div[id="x_axis"]').click()
  cy.contains("Day").click()
  cy.get('input[id="series_title"]').type("Usage time by day this week")
  cy.get('div[id="y_axis"]').click()
  cy.contains("Total Duration").click()
  cy.get('div[id="subgroup_by"]').click()
  cy.contains("Gender").click()
  cy.get('div[id="chart_type"]').click()
  cy.contains("Bar Chart").click()
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
  cy.contains("Test1").should("exist")
  cy.get("svg[data-testid='DeleteIcon']").should("exist")
 })

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
})