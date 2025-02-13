# Admin can move content

## Description:

An attempts list on content page shows the completion status or score of user attempts.

## Step-by-Step Procedure:

### Part 1: Admin Setup
1. Import Users:
   Use the importUsersViaHttp function to import users from the "Ustad_Teacher_and_Students. csv"  file.
2. Admin Login:
   Log in to the application as the Admin user (admin/testpass).
3. Add Content to Library:
   Add the "Video_Content.mp4" file to the content library with the name "Content_001".
4. Add Content Block to Course:
   * Navigate to the "Courses" section. 
   * Open the "Test Course Block" course. 
   * Click the "Edit" button. 
   * Click "Add block". 
   * Select "Content". 
   * In the content entry filter, click "Library". 
   * Select "Content_001" from the library. 
   * Set the maximum points to "10". 
   * Click "Done". 
   * Click "Save".
5. Add EPUB Content Block:
   * Navigate to the "Course" section. 
   * Open the "Test Course Block" course. 
   * Click the "Course" button. 
   * Click the "Edit" button. 
   * Click "Add block". 
   * Click "Add content block". 
   * Click "Import from file". 
   * Select the "Epub_Content1.epub"  file. 
   * Click "Next". 
   * Set the content block title to "Content_002". 
   * Set the maximum points to "10". 
   * Click "Done". 
   * Click "Save".
6. Verify Content Blocks:
   * Navigate to "Courses". 
   * Open "Test Course Block". 
   * Verify that both "Content_001" and "Content_002" are visible in the course.
### Part 2: Student Interactions
7. Student 1 - Video Attempt:
   * Log in as Student 1 (stud1/tests1).
   * Navigate to "Test Course Block". 
   * Click on "Content_001" (the video content). 
   * Click "OPEN". 
   * Verify that the video player is displayed. 
   * Play the video.
8. Student 2 - EPUB Attempt 1:
   * Log in as Student 2 (stud2/tests2). 
   * Navigate to "Test Course Block". 
   * Click on "Content_002" (the EPUB content). 
   * Click "OPEN". 
   * Verify that the EPUB content is displayed. 
   * Verify that the text "THE ADOPTING OF ROSA MARIE" is visible. 
   * Verify that the text "The Adopting of Rosa Marie / (A Sequel to Dandelion Cottage)" is visible. 
   * Click on "THE PERSONS OF THE STORY".
9. Student 3 - EPUB Attempt 1:
   * Log in as Student 3 (stud3/tests3). 
   * Navigate to "Test Course Block". 
   * Click on "Content_002" (the EPUB content). 
   * Click "OPEN". 
   * Verify that the EPUB content is displayed. 
   * Verify that the text "THE ADOPTING OF ROSA MARIE" is visible. 
   * Verify that the text "The Adopting of Rosa Marie / (A Sequel to Dandelion Cottage)" is visible. 
   * Scroll to and click on "CHAPTER VII Discovery".
10. Student 2 - EPUB Attempt 2:
    * Log in as Student 2 (stud2/tests2). 
    * Navigate to "Test Course Block". 
    * Click on "Content_002" (the EPUB content). 
    * Click "OPEN". 
    * Verify that the EPUB content is displayed. 
    * Verify that the text "THE ADOPTING OF ROSA MARIE" is visible. 
    * Verify that the text "The Adopting of Rosa Marie / (A Sequel to Dandelion Cottage)" is visible. 
    * Scroll to and click on "CHAPTER XXX An April Harvest".
### Part 3: Verify Attempt Lists
11. Student 1 - View Attempts:
    * Log in as Student 1 (stud1/tests1). 
    * Navigate to "Courses". 
    * Open "Test Course Block". 
    * Click on "Content_001". 
    * Click the "Attempts" button.
  Verify:
    * "Attempts: 1" is displayed.
    * "100% Completion" is displayed.
    * "0% Score" is displayed. 
    * Click on "Student 1".
    * "Completed" is displayed.
    * "100% Completion" is displayed.
12. Student 2 - View Attempts:
    * Log in as Student 2 (stud2/tests2). 
    * Navigate to "Courses". 
    * Open "Test Course Block". 
    * Click on "Content_002". 
    * Click the "Attempts" button.
  Verify:
    * "Attempts: 2" is displayed.
    * "62% Completion" is displayed.
    * "0% Score" is displayed. 
    * Click on "Student 2".
    * "Incomplete" is displayed. 
    * A timer icon is displayed.
    * "62% Completion" is displayed. 
    * Click on "Incomplete".
    * 2 "Experience-" should be displayed.
    * When user click on Experience filter chip
    * Experience should be visible
13. Student 3 - View Attempts:
    * Log in as Student 3 (stud3/tests3). 
    * Navigate to "Courses". 
    * Open "Test Course Block". 
    * Click on "Content_002". 
    * Click the "Attempts" button.
  Verify:
    * "Attempts: 1" is displayed.
    * "25% Completion" is displayed.
    * "0% Score" is displayed. 
    * Click on "Student 3".
    * "Incomplete" is displayed. 
    * A timer icon is displayed.
    * "25% Completion" is displayed. 
    * Click on "Incomplete".
    * "Experience-" is displayed.
14. Teacher - View Attempts:
    * Log in as Teacher 1 (teach1/testt1). 
    * Open "Test Course Block". 
    * Click on "Content_002". 
    * Click the "Attempts" button.
  Verify:
    * "62% Completion" is displayed.
    * "0% Score" is displayed.
    * "Attempts: 1" is displayed. 
    * Click on "Student 2".
    * "Incomplete" is displayed. 
    * A timer icon is displayed.
    * "62% Completion" is displayed. 
    * Click on "Incomplete".
    * "Experience-" is displayed. 
    * Navigate to "Courses". 
    * Open "Test Course Block".
    * Open "Test Course Block". 
    * Click on "Content_002". 
    * Click the "Attempts" button.
  Verify:
    * "Student 3" is displayed.
    * "25% Completion" is displayed.
    * "0% Score" is displayed.
    * "Attempts: 1" is displayed.
    * Click on "Student 3".
    * "Incomplete" is displayed.
    * A timer icon is displayed.
    * "25% Completion" is displayed.
    * Click on "Incomplete".
    * "Experience-" is displayed.
15. Student 1 - Not able to view Attempts made by Student 2:
    * Log in as Student 1 (stud1/tests1).
    * Navigate to "Courses".
    * Open "Test Course Block".
    * Click on "Content_002".
    * Click the "Attempts" button.
  Verify:
    * "Nothing here, yet" is visible