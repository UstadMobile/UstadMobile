# Assignment submission page is open after the deadline the submit button won't be visible.

## Description:

If assignment submission page is open after the deadline the submit button should not be visible.

## Step-by-Step Procedure:

1. Using http api endpoint created a class with students and teachers already enrolled and setup.
2. Login to the app using the teacher username and password.
3. Click on "Courses" tab.
4. Click on the course name.
5. Click on "Edit" button.
6. Click on "Add block" button
7. Click on "Assignment".
8. Enter the assignment title in the "Title" field.
9. Enter the assignment description in the "Description" field.
10. Enter yesterday's date and time in "Deadline" field. 
11. Enter today's date and set the time to 1 hour minus the current time in the "End of grace period" field.
12. Enter a number in "Late submission penalty" field.
13. Set the submission policy to "Must submit all at once".
14. Click on the "Done" button.
15. Click on the "Save" button.
16. Clear the app storage
17. Login to the app using the student username and password.
18. Click on "Courses" tab.
19. Click on the course name.
20. Click on the assignment added by teacher.
21. Verify that the "Submit" button is not visible.