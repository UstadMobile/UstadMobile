# Admin or teacher can add a content block by linking existing content or via file upload

## Description:

Admin or teacher can add a content block by link existing content or add as a new content from file. Block can be edited (title, description, other metadata, not the content itself within app) or deleted from course.

## Step-by-Step Procedure:

1. Using http api endpoint created a class with students and teachers already enrolled and setup.
2. Login as Admin.  
3. Download the test content file. 
4. Admin - Add a Content to Library
   * Click on "Library."
   * Click on the "+ Content" button. 
   * Click on the "From file" or "From folder" button. 
   * Select the content file. 
   * Click the "Save" button. 
   * Verify that the content is visible in the library. 
5. Teacher - Add a existing content in library to a course
   * Login to the app
   * Go to courses
   * Open the existing course and click edit button
   * Click "+ Add Block" button and select "Content" option
   * Navigate to "Library." tab
   * Select the file inside the library
   * Save the content block and the course
6. Teacher - Add a new content block to a course from file
   * Navigate to "Courses" and open "Test Course Block."
   * Click "+ Add Block" and select "Content."
   * Select "My Content" and choose the test content. 
   * Click "Save."
   * Verify content appears in the course.
7. Student 1 - Attempt Video Content
   * Login as Student 1. 
   * Navigate to "Test Course Block."
   * Scroll to "Video" and tap "Open."
   * Tap "Play" and wait for playback to start. 
   * Return to "Attempts" section. 
   * Verify completion status: "100% completion."
   * Click on "Student 1" to view attempt details. 
   * Verify progress and completion percentage.
8. Student 2 - Attempt Interactive Content (H5P/PDF/EPUB)
   * Login as Student 2. 
   * Navigate to "Test Course Block."
   * Open "Interactive Content" (e.g., H5P/PDF/EPUB). 
   * Interact with the content (e.g., answer question). 
   * Submit response and verify score. 
   * Go to "Attempts" and verify attempt is recorded.
9. Student 3 - Attempt Content
   * Login as Student 3. 
   * Navigate to "Test Course Block."
   * Open "Interactive Content."
   * Interact and submit response. 
   * Verify attempt is recorded in "Attempts."
   * Click on "Student 3" and check details.
   * Verify the filter chips are selected and corresponding progress bars are visible by default
   * Unselect "Answered" filter chip and verify that "Answered" progress bar is not visible
10. Teacher - Verify Student Attempts
   * Login as Teacher. 
   * Navigate to "Test Course Block."
   * Open "Attempts" section. 
   * Verify Student 2 and Student 3 attempts are listed. 
   * Click on "Student 3" to view progress details. 
   * Verify recorded score and completion percentage. 
   * Click on "Student 2" and verify attempt details. 
11. Student 1 - Verify No Other Student’s Attempts visible
   * Login as Student 1. 
   * Navigate to "Test Course Block."
   * Go to "Attempts" section. 
   * Verify no attempts from Student 2 or Student 3 are visible. 
   * Ensure "Nothing here, yet" is displayed.


