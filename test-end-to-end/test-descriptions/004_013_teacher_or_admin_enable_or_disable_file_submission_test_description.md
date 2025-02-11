# Admin or teacher can enable or disable the file submission and can limit maximum file size

## Description:

Students may upload attachments for assignment submissions if enabled by the teacher, up to the maximum file size limit set by the teacher.

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
10. Verify the "Require file submission" switch is on
11. Set the "Size limit(MB)" from 50 to 10 
12. Set the submission policy to "Must submit all at once". 
13. Click on the "Done" button. 
14. Click on the "Save" button. 
15. Clear the app storage 
16. Login to the app using the student username and password. 
17. Click on "Courses" tab. 
18. Click on the course name.
19. Click on the assignment added by teacher. 
20. Enter the assignment text in the provided text field. 
21. Click on "Add file" button. 
22. Select the file of size 30 MB 
23. User gets error message "File size is too big". 
24. Click on "Add file" button. 
25. Select the file of size 5 MB. 
26. Click on the "Submit" button. 
27. Verify the submit status changed to "Submitted". 
28. Clear the app storage 
29. Login to the app using the teacher username and password. 
30. Click on "Courses" tab. 
31. Click on the course name. 
32. Click on the assignment. 
33. Click on the "Submissions" tab. 
34. Click on the specific student's name.
35. Review the submitted text 
36. Open the submitted file. 
37. Click on "Mark" field. 
38. Enter the mark 
39. Click on the "Submit grade" button.