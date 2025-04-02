# Teacher and admin user can copy a course.

## Description:

Teacher and admin user can copy an existing course, including assignments, videos, and other materials, so they can quickly create a new version without setting everything up from scratch

## Step-by-Step Procedure:

1. Import user data from "Ustad_Teacher_and_Students.csv".
2. Log in as an admin user with credentials 'admin' and 'testpass'.
3. Click on 'Test Course Block'.
4. Click on the 'Edit' button.
5. Click on 'Add block'.
6. Select 'Module'.
7. Enter 'Term 1' in the title input field.
8. Click the 'Done' button.
9. Verify that 'Term 1' is visible.
10. Click the 'Save' button.
11. Click the 'Edit' button again.
12. Click on 'Add block'.
13. Select 'Assignment'.
14. Enter 'Assignment 1' in the title input field.
15. Set the 'cbDeadlineDate' to date in last year.
16. Click the 'Done' button.
17. Verify that 'Assignment 1' is visible.
18. Click on 'Add block'.
19. Select 'Discussion Board'.
20. Enter 'Discussion 1' in the title input field.
21. Click the 'Done' button.
22. Set 'clazz_start_time' to date in past year.
23. Set 'clazz_end_time' to last year date.
24. Click the 'Save' button.
25. Verify that 'Assignment 1' is visible.
26. Click on 'Discussion 1'
27. Click on "+ Post" button
28. Enter post title and content
29. Tapon "Save"
30. Verify the post is visible
31. Go to course overview page
32. Click on 'Copy' button.
33. Verify that "Copy course" is visible in the app bar title.
34. Verify that the input field contains "Copy of Test Course Block".
35. Verify that 'Term 1' exists.
36. Verify that 'Assignment 1' exists.
37. Click on 'Assignment 1'.
38. Set 'cbDeadlineDate' to Tomorrow.
39. Click on '#caSubmissionPolicy'.
40. Select 'Can make multiple submissions'.
41. Click on the 'Done' button.
42. Clear and enter 'New Test Course' in the 'clazz_name' input field.
43. Set 'clazz_start_time' to Yesterday.
44. Clear 'clazz_end_date'.
45. Click on 'Add block'.
46. Select 'Text'.
47. Enter 'Text 1' in the title input field.
48. Click the 'Done' button.
49. Verify that 'Text 1' is visible.
50. Click the 'Save' button.
51. Click on 'Discussion 1'
52. Verify the 'Post 1' is not visible
53. Click on 'Courses' tab.
54. Verify that 'New Test Course' exists.
55. Log in as a teacher with credentials 'teach1' and 'testt1'.
56. Click on 'All'.
57. Verify that 'Test Course Block' exists.
58. Click on 'Test Course Block'.
59. Verify that 'Copy' button does not exist.
