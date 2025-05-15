# Assignment is set to be marked by peers and submitted by groups

## Description:

Teacher creates a group assignment with peer marking. Group members submit the assignment. Assigned peers review and rate submissions. Admin copies the course and verifies that peer submissions and reviews are not carried over.
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
19. Click on "Marked by" drop down.
20. Select "Peers" option.
21. Enter 1 in "Reviews per user or group" field.
22. Click on "Assign Reviewers" button.
23. Click on "Assign random reviewers" button. 
24. Click on the "Done" button to save reviewers.
25. Click on the "Done" button to save assignment
26. Click on the "Save" button to save course.
27. Clear the app storage 
28. Login to the app using the student username and password in group 1. 
29. Click on "Courses" tab. 
30. Click on the course name. 
31. Click on the assignment added by teacher. 
32. Enter the assignment text in the provided text field. 
33. Click on the "Submit" button. 
34. Verify the submit status changed to "Submitted". 
35. Clear the app storage 
36. Login to the app using the student username and password in group 2. 
37. Click on "Courses" tab. 
38. Click on the course name. 
39. Click on the assignment added by teacher. 
40. Click on the "Peers to review" tab.
41. Click on "Group 1".
42. Review the Group 1 submitted text. 
43. Click on "Mark" field. 
44. Enter the mark 
45. Click on the "Submit grade" button.
46. Clear the app storage 
47. Login to the app using the student username and password in group 1. 
48. Click on "Courses" tab. 
49. Click on the course name. 
50. Click on the assignment. 
51. The student should see their graded assignment and the marks given by the peer.
52. Clear storage and open the app
53. Login as admin user with credentials 'admin' and 'testpass'.
54. Click on "Test Course Block".
55. Click "Copy".
56. Verify "Copy course" title is visible.
57. Verify input field contains "Copy of Test Course Block".
58. Click "Save".
59. Wait until save is complete.
60. Click "Courses".
61. Click on "Copy of Test Course Block".
62. Open "Assignment 1".
63. Click on "Submissions".
64. Verify that "Group 1" and "Group 2" are not visible (submission data not carried over).

