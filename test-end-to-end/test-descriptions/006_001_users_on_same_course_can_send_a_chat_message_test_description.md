# Users on same course can send a chat message

## Description:

Users can send a chat message (plain text) to any other user that they have permission to see (teachers and students can see those who are part of their courses by default). The recipient can reply to the message. Messages/replies are delivered instantly when the app is open. Note: Messages are only delivered if the app is open and notifications are not displayed.

## Step-by-Step Procedure:

1. Using http api endpoint created a class with students and teachers already enrolled and setup.
2. Launch ustad app
3. Click on "Existing User" button
4. Click on "Enter link manually" button
5. Enter the site link and click Next
6. Login to the app using the teacher username and password. 
7. Click on "Messages" tab at bottom 
8. Click on "+ Message" button 
9. Select a student from student list. 
10. Enter message in "Message" tab 
11. Click on "Send" icon. 
12. Verify the message is visible. 
13. Clear the app storage 
14. Login to the app using the student username and password.
15. Click on "Messages" tab at bottom
16. Verify student able to see message from teacher
17. Click on teacher name to open the message
18. Verify user able to see the message from teacher with date and time.
19. Enter message in "Message" tab to reply 
20. Click on "Send" icon.
21. Clear the app storage 
22. Login to the app using the teacher username and password. 
23. Click on "Messages" tab at bottom 
24. Verify teacher able to see reply message from the student.