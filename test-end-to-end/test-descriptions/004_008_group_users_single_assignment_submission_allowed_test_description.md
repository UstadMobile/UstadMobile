# Group assignment is set that only one submission is allowed

## Description:

If group assignment is set that only one submission is allowed, when the students make a submission, they will not be able to make any further submission (including as another member of the group)

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
10. Click on "Group Submission" switch
11. Click on "Groups" field.
12. Click on "Add new groups".
13. Enter a group title in "Title field"
14. Enter 2 in "Number of groups" field.
15. Assign first 3 students to Group 1.
16. Assign last 3 students to Group 2.
17. Click on "Save" button to save group details.
18. Set the submission policy to "Must submit all at once".
19. Click on the "Done" button.
20. Click on the "Save" button. 
21. Clear the app storage 
22. Login to the app using the student username and password in group 1. 
23. Click on "Courses" tab. 
24. Click on the course name. 
25. Click on the assignment added by teacher.
26. Enter the assignment text in the provided text field.
27. Click on the "Submit" button.
28. Verify the submit status changed to "Submitted".
29. Clear the app storage 
30. Login to the app using the another student username and password in same group 1. 
31. Click on "Courses" tab. 
32. Click on the course name. 
33. Click on the assignment added by teacher. 
34. Verify the "Text" ,"Add file" and "Submit" buttons are not visible to user since it is single submission.
