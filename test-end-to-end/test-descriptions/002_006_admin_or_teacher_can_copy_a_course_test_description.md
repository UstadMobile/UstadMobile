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
15. Set the 'hide_until_date' to "2023-12-01T08:30".
16. Set the 'cbDeadlineDate' to "2024-01-01T08:30".
17. Click the 'Done' button.
18. Verify that 'Assignment 1' is visible.
19. Set 'clazz_start_time' to "2023-11-01".
20. Set 'clazz_end_time' to "2024-11-01".
21. Click the 'Save' button.
22. Verify that 'Assignment 1' is visible.
23. Verify that 'Members' is visible.
24. Log in as a teacher with credentials 'teach1' and 'testt1'.
25. Click on 'All'.
26. Verify that 'Test Course Block' exists.
27. Click on 'Test Course Block'.
28. Click on 'Copy'.
29. Verify that "Copy course" is visible in the app bar title.
30. Verify that the input field contains "Copy of Test Course Block".
31. Verify that 'Term 1' exists.
32. Verify that 'Assignment 1' exists.
33. Click on 'Assignment 1'.
34. Set 'hide_until_date' to "2025-01-01T08:30".
35. Set 'cbDeadlineDate' to "2025-10-01T08:30".
36. Click on '#caSubmissionPolicy'.
37. Select 'Can make multiple submissions'.
38. Click on '#caClassCommentEnabled'.
39. Verify that the 'Done' button is visible.
40. Click on the 'Done' button.
41. Clear and enter 'New Test Course' in the 'clazz_name' input field.
42. Set 'clazz_start_time' to "2025-01-01".
43. Set 'clazz_end_time' to "2026-01-01".
44. Click the 'Save' button.
45. Click on 'Courses'.
46. Verify that 'New Test Course' exists.
47. Log in as a student with credentials 'stud1' and 'tests1'.
48. Click on 'All'.
49. Verify that 'Test Course Block' exists.
50. Click on 'Test Course Block'.
51. Verify that 'Copy' does not exist.
