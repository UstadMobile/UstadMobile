# Admin can enable/disable registration for users and manage visibility of buttons in bottom navigation bar

## Description:

Admin can enable/disable registration for users and manage visibility of buttons in bottom navigation bar

## Step-by-Step Procedure:

1. Using http api endpoint created a class with students and teachers already enrolled and setup.
2. Launch Ustad app
3. Click on "Existing User" button
4. Click on "Enter link manually" button 
5. Enter the link in "Site link" field 
6. Click on "Next" button 
7. Enter admin credentials and login 
8. Click on "Settings" icon. 
9. Click on "Site" button. 
10. Click on "Edit" button. 
11. Enter Company terms in "Terms and policies" text field. 
12. Click on "Registration allowed" switch. 
13. Click on "Save" button 
14. Clear app storage. 
15. Open the app 
16. Click on the "New User"
17. Click on the "Join Learning Space"
18. Click on "Enter link manually"
19. Enter the site link in "Site link" field. 
20. Click on the "Next" button 
21. Click on the "Next" button on select Date Of Birth page
22. Verify user gets an error "This field is required."
23. Select the date of birth, age 13+ 
24. Click on the "Next" button 
25. Verify the entered "Terms and policies" visible to user.
26. Click on the "Accept" button
27. If platform supports passkeys, click on "Other options"
28. Verify user gets an error "This field is required." just below Full name and gender fields.
29. Enter Full name.
30. Select Gender
31. If platform supports passkeys, click on "Other options".
32. Click on "Signup" button
33. Verify user gets an error "This field is required." just below Username and password fields.
34. Enter username and password
35. Click on "Signup" button
36. User logged into the app
37. User directed to courses section
38. Verify user able to view Courses, Messages and People tabs
39. Clear app storage. 
40. Open the app 
41. Click on "Existing user" button 
42. Click on "Learning Space" button 
43. Click on "Enter link manually" button 
44. Enter the link in "Site link" field 
45. Click on "Next" button 
46. Enter admin credentials and login 
47. Click on "Settings" icon. 
48. Click on "Site" button. 
49. Click on "Edit" button. 
50. Click on "Registration allowed" switch to disable registration.
51. Click on "Save" button 
52. Clear app storage. 
53. Click on the "New User"
54. Click on the "Join Learning Space"
55. Click on "Enter link manually"
56. Enter the site link in "Site link" field. 
57. Click on the "Next" button 
58. Verify user able to see an error saying this learning space does not allow new user registration. 
59. Clear app storage.
60. Open the app
61. Click on "Existing user" button
62. Click on "Learning Space" button
63. Click on "Enter link manually" button
64. Enter the link in "Site link" field
65. Click on "Next" button
66. Enter admin credentials and login
67. Click on "Settings" icon.
68. Click on "Site" button.
69. Click on "Edit" button.
70. Click on "People" switch to hide "People" tab in bottom navigation.
71. Click on "Messages" switch to hide "Messages" tab in bottom navigation.
72. Click on "Save" button
73. Verify Admin only able to see Courses and Library tabs in bottom navigation bar 
74. Clear app storage.
75. Open the app
76. Click on "Existing user" button
77. Click on "Learning Space" button
78. Click on "Enter link manually" button
79. Enter the link in "Site link" field
80. Click on "Next" button
81. Enter student credentials and login
82. Verify student able to see Courses and Library tabs in bottom navigation bar
83. Verify student not able to see Messages and People tabs in bottom navigation bar
84. Clear app storage.
85. Open the app
86. Click on "Existing user" button
87. Click on "Learning Space" button
88. Click on "Enter link manually" button
89. Enter the link in "Site link" field
90. Click on "Next" button
91. Enter teacher credentials and login
92. Verify teacher able to see Courses and Library tabs in bottom navigation bar
93. Verify teacher not able to see Messages and People tabs in bottom navigation bar