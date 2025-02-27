# Users can generate a bar chart report.

## Description:

Admin or teacher can add or customize reports so that they can track progress.

## Step-by-Step Procedure:

1. Prepare Data: Use an HTTP API endpoint to create a class with students and teachers already enrolled, ensuring there is data for report testing.
2. Login: Access the app using a teacher's username and password.
3. Navigate to Reports: Click on the "Reports" tab.
4. Add a Report: Click on the "Add report" button.
5. Enter Report Details:
   * Set the report title as "Test1".
   * Select "Custom date range" as the time range.
   * Set the Start date to 1 month before today and End date to today.
6. Configure Chart Data:
   * Select X-axis as "Day".
   * Enter the Series Title as "Usage time by day this week".
   * Select Y-axis as "Total Duration".
   * Select Sub-group by as "Gender".
   * Choose Chart Type as "Bar Chart".
7. Apply a Filter:
   * Click on "Add filter".
   * Set the Field to "Person Age".
   * Select Condition as "Greater than" ( > ).
   * Enter Value = 13.
   * Click Done.
8. Generate Report:
   * Click Done to finalize the report.
   * Verify that the bar chart is displayed correctly.
9. Modify Report:
   * Click Edit on the generated report.
   * Change the time range to "Custom period (e.g. last X days/weeks)".
   * Change the Chart Type to "Line Chart".
   * Click Done.
   * Verify the line chart is displayed correctly.
10. Verify Report Existence:
   * Click on "Reports".
   * Ensure the report named "Test1" exists.
   * Confirm that the delete icon is present, allowing the user to remove the report.
