# Admin or teacher send invitation via phone numbers or email addresses to a new user

## Description:

Teacher (course leader) or admin can send an invitation to a list of users by providing phone numbers (SMS) or email addresses. User can open the invitation, create a new account if this is allowed by admin policy and they don't have an existing account, and join a course.

## Step-by-Step Procedure:

1. Using http api endpoint created a class with a teacher and unenrolled students setup.
2. Launch Ustad app
3. Click on "Existing user" button
4. Click on "Learning Space" button
5. Click on "Enter link manually" button
6. Enter the link in "Site link" field
7. Click on "Next" button
8. Login as admin.
9. Click on "Courses.
10. Click on the course name.
11. Click on the "Members" tab.
12. Click on the "Invite Students" button.
13. Enter student email separated by commas (eg: stud1@email.com,stud2@email.com).
14. Click "Send."
15. Admin Click on "Settings" icon.
16. Click on "Site" button.
17. Click on "Edit" button.
18. Enter Company terms in "Terms and policies" text field.
19. Click on "Registration allowed" switch.
20. Click on "Save" button
21. Verify that the students receive an email from Ustad Mobile using http call (serverUrl + "api/testemail/list?to=" + email).
22. Students open the email and click the provided link.
23. The link opens the app
24. User gets "Existing user or New User" screen
25. Click on the "New User"
26. Click on the "Join Learning Space"
27. Click on "Enter link manually"
28. Enter the site link in "Site link" field.
29. Click on the "Next" button
30. Select the date of birth, age 13+
31. Click on the "Next" button
32. Read the "Terms and policies"
33. Click on the "Accept" button
34. Enter Full name.
35. Select Gender
36. If platform supports passkeys, click on "Other options", then signup with username and password.
37. Enter username and password
38. Click on "Signup" button
39. User logged into the app
40. User gets an invitation page that prompts the user to join the course.
41. The student clicks "Accept."
42. Verify that the student appears in the course members list.
43. Clear storage
44. Login as a teacher
45. Go to courses
46. Open the course and verify the student 1 is enrolled to the course

