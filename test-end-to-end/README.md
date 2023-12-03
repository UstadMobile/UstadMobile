# End-to-end tests

End-to-end tests that start a blank new server/app and test functionality end-to-end.

* [android-maestro](android-maestro/) Android end-to-end tests built using [Maestro](https://maestro.mobile.dev)
* [webapp-cypress](webapp-cypress/) Web end-to-end tests built using [Cypress](https://www.cypress.io/)

## Tests

### 1 : Library

1.1 Admin can add H5P, video, EPUB and Experience API zip files from the device and by using a link. 
    Once added the content can be opened. If added on Mobile or Desktop app from the device, testing 
    opening should be from another device or after clearing the app on the device used to upload it.

1.2 If admin uploads an invalid file where the file extension / mime type indicate that it should be 
    a supported file, but the file content is not valid (e.g. a file with the name video.mp4 that is 
    not actually a video), then an error message will be displayed to notify that the content is 
    invalid.

1.3 Admin can hide and unhide content. After hiding on admin device, the content is hidden on other device.

1.4 Admin can move content to another folder in Library. After moving, content is shown as moved on other device. 

1.5 Content can be deleted from a device. After deletion, it can be downloaded again and opened as 
    before. (Android only).

1.6 Content download can be canceled. After cancellation, the download stops and it can be 
    downloaded again. (Android only).

1.7 If a download is started on WiFi and WiFi is stopped midway, the download will stop and then 
    resume when WiFi is resumed. (Android only).

1.8 H5P and video content can be automatically compressed using high, medium, and low quality presets. 

1.9 Admin can delete content from library. Deleting content will remove the files associated with the content from the server. Client devices will recognize any files that are associated with deleted content as eligible for eviction from the cache.

1.10 User can select (in settings) to store content on device storage or memory card (if available - Android apps can only use storage on memory cards that are reformatted on the device to use as storage (not FAT32 etc)). Any subsequent download is saved on the selected storage medium. (Android only).

### 2: Course creation, enrolment, attendance, progress

2.1 Admin can create a new course, add a new teacher, and create an account for that teacher. It is 
    possible to login as the teacher.

2.2 Teacher can login. Students can join class using class code or link.

2.3 Teacher can record attendance. Teacher can edit attendance after recordings

2.4 Teacher can view progress report showing each students' result for each block in the course.

2.5 The course title is mandatory. If save is clicked when the title is blank, then the field is
    highlighted as required and it is not saved. The user can then add a title and save successfully.

2.6 Teacher/admin can remove people from a course (e.g. delete their enrolment). This removes any permissions that were associated with their enrolment.    

2.7 Teacher and admin user can grant permission for the course to other users. Permissions that can be granted include view attendance, edit attendance, view learning records, edit learning records, view details (name, date of birth, phone number etc) of members, edit details of members, view course, edit course.

2.8 Teacher/admin can add banner photo to the course that is displayed in the course list screen and course detail screen.

2.9 Teacher/admin, or the user themselves, can select or remove a profile photo. Photo can be taken from camera or gallery on Android.

### 3: Course block editing

3.1 Admin or teacher can edit the course. Other users cannot edit

3.2 Admin or teacher can add module and text blocks. Blocks can be reordered and hidden, indented, 
    unindented, edited, deleted.

3.3 Admin or teacher can add a content block and link existing content. Block can be edited (title, description, other metadata, not the content itself within app) or deleted from course.

3.4 Admin or teacher can add a content block and upload new content via link

3.5 Admin or teacher can add a content block and upload new content via file upload

3.6 All users on course should be able to view the course blocks as they were added. Modules can 
    expand/collapse.

3.7 All users on course should be able to open text blocks to see all text.

3.8 The course block title is mandatory. If save is clicked when the title is blank, then the field is
highlighted as required and it is not saved. The user can then add a title and save successfully.


### 4: Assignments

4.1 Teacher can create assignment (set to allow one submission per student as per default), student 
    can submit assignment (text and attachment), teacher can view submission and grade it. After 
    submitting their work, the submit button is no longer visible. Student will see their mark when 
    it is graded. Assignment can be edited or deleted from course.

4.2 Teachers and students can add course comments which are visible for all who can view the assignment.

4.3 Teachers and students can submit and view private comments. Student can submit a private comment, 
    teacher can see the private comment, and reply to it. 

4.4 If assignment is set to allow multiple submissions, student can make another submission after 
    the first submission. Teacher can see the revised submission and give a new grade. The student 
    can see the updated grade.

4.5 If assignment is submitted after the deadline but before the grace period, the submission should
    be accepted. The specified late penalty should be applied to the mark given by the teacher.

4.6 If assignment submission page is open after the deadline the submit button should not be visible.

4.7 If assignment submission page is open before the deadline, and the deadline passes whilst the 
    screen is open, the student should not be able to submit.

4.8 Teacher can create assignment as per 4.1 by groups. When another group member logs in, the group member will see the same submission and comments.

4.9 Teacher and students can submit and view private comments. Students who are in the same group see the same set of comments. Comments are private within the group.

4.10 If group assignment is set that only one submission is allowed, when the students make a submission, they will not be able to make any further submission (including as another member of the group)

4.11 If assignment is set to allow multiple submissions, student can make another submission after the first submission. Teacher can see the revised submission and give a new grade. The student can see the updated grade (including as another member of the group).

4.12 If assignment is set to be marked by peers, where each assignment is marked by two peers, students can see assignment submissions for those that they have been assigned to mark. Students can mark each others work according to the peer review allocation. Students will see the marks submitted by their peers.

4.13 If assignment is set to be marked by peers and submitted by groups, where each assignment is marked by two peers, students can see assignment submissions for those that their group has been assigned to mark. Students can mark each others work according to the peer review allocation (as per group membership). Students will see the marks submitted by their peers (other groups).

4.14 Teacher/admin (any user with permission to edit the course itself) can delete private comments and course comments.

4.15 Students may upload attachments for assignment submissions if enabled by the teacher, up to the maximum file size limit set by the teacher. 

### 5	Discussion board
5.1 Teacher can add discussion board to course

5.2 Teacher can edit discussion board

5.3 All users on course can post on discussion board, see posts from other users. Each topic and post shows last active date and number of replies.

5.4 Users can include a link from within the app (e.g. to a course or content piece) in their post. Clicking the link opens the item directly.

5.5 Users can include external links to websites in their post. Clicking the link opens the browser (e.g. Chrome on Android, new tab in web) for the link

5.6 Teacher/admin (any user with permission to edit the course itself) can delete posts and replies. 

### 6 Messaging

6.1 Users can send a chat message (plain text) to any other user that they have permission to see (
    teachers and students can see those who are part of their courses by default). The recipient can
    reply to the message. Messages/replies are delivered instantly when the app is open. Note: 
    Messages are only delivered if the app is open and notifications are not displayed.

6.2 When a user receives a chat message from a user that they could not normally see (e.g. when the
    admin sends them a message), they will be able to see the name of the sender.

	
### 7	User accounts:

7.1 Admin can enable/disable registration for users

7.2 When registration is enabled: Users over 13 can register directly

7.3 When registration is enabled: Users under 13 can register using parental approval link

7.4 When registration is enabled: If a user does not specify their date of birth, an error message 
    is displayed and they cannot proceed until they enter their date of birth.

7.5 If a user under 13 is added by an adult (e.g. teacher/admin) approval is not required

7.6 Admin can enable/disable guest login. If enabled, then users can connect as guest

7.7 Teacher (course leader) or admin can send an invitation to a list of users by providing phone 
    numbers (SMS) or email addresses. User can open the invitation, create a new account if this is 
    allowed by admin policy and they don't have an existing account, and join a course.

7.8 First name, last name, and gender are mandatory fields. If the first name or last name is blank,
    then the field is highlighted as mandatory. If the gender is unset (the initial default), the 
    field is highlighted as mandatory. The user can only save the person profile once mandatory 
    fields are completed.

7.9 If the email is not blank, it must be a valid email address. To be considered valid, the email
    address must 1) contain one @ character 2) not contain prohibited characters (white space, [, ], 
    \).

7.10 If the phone number is not blank, it must contain a valid phone number. Phone number validation
     will be performed by libphonenumber which understands location-specific rules on number length
     etc.

7.11 If a user is logged into the Ustad app, they can use an api consumer (e.g. UstadApiConsumerDemo), 
    use the single sign-on, see a list of logged in accounts, select their account, click approve, 
    and receive an auth token.

7.12 If no user is logged into the Ustad app, they can use an api consumer (e.g. UstadApiConsumerDemo), 
     use the single sign-on, enter the site link, login with their username/password, then click approve, 
     and receive an auth token.

### 8 App panic response (Android only)

8.1 When a panic trigger app is installed and selected, when the app is set to delete all data on panic trigger and the panic app is triggered, then all local data is deleted and the user is logged out.

8.2 When a panic trigger app is installed and selected, when the app is set to hide on panic trigger, the app launch disappears from the home screen. A notepad app is visible instead. The app is restored by entering the code into the notepad.

### 9 Peer-to-peer (Android and desktop only)

9.1 Users who do not have an Internet connection can connect via a nearby device with the app 
    installed to login and use the platform (e.g. open course, view course members, etc). This will 
    work if the nearby device has synced the data required by the user without an Internet connection
    or itself has access to an Internet connection.

8.2 Users who do not have an Internet connection can download content via a nearby device from the 
    device itself instead of from the Internet. This will work if the nearby device has synced the 
    data required by the user without an Internet connection or if the nearby intermediary device
    itself has an Internet connection.

### 10 Accessibility

10.1 Video content can have subtitles added using an WebVTT file. The subtitles will be displayed 
    with the video if uploaded.

10.2 Screens support the use of text-to-speech as provided by the operating system (e.g. Windows, 
    Android). Image buttons are labeled appropriately for speech readers.

### 11 Administration

11.1 Server can be installed via script.

11.2 Server data can be backed up via script.

### 12 Language support

12.1 When first starting the app it will load in the default system language if the system is set to a supported language.

12.2 If the system is set to an unsupported language, the app will be displayed in English (fallback language).

12.3 After login, user can go to settings, and select any supported language, or to use the system device language. The user interface will change to the selected language. The language setting is applied to the whole app, not per-user (if a user switched accounts, the language does not change - language setting is stored locally).

12.4 Android: Android 13+ users can select language in app settings using [per-app language preferences](https://developer.android.com/guide/topics/resources/app-languages) 


### 13 Reporting

13.1 Users can generate a bar chart or line chart.

Y-axis data options:

a. Content usage - total duration<br/>
b. Content usage - average duration per session<br/>
c. Content usage - number of sessions<br/>
d. Content usage - number of interactions recorded<br/>
e. Content usage - number of active users<br/>
f. Content usage - average usage time per user<br/>
g. Content usage - number of students who completed content<br/>
h. Content usage - percentage of students who completed content (out of those who attempted)<br/>
i. Attendance - total attendances<br/>
j. Attendance - total absences<br/>
k. Attendance - total lates<br/>
l. Attendance - Percentage of students who attended<br/>
m. Attendance - Percentage of students who attended or were late<br/>
n. Attendance - Total number of classes<br/>
o. Attendance - Number of unique students attending.<br/>

X-axis options: day, week, month, content entry, gender, class, class enrolment outcome, class enrolment reason for leaving<br/>

Subgroup options: content entry (content usage reports only), gender, course, enrolment outcome (attendance reports only), reason for leaving (attendance reports only)

Filter options: gender, completion status, content completion status, content entry,  content progress, attendance percentage, class enrolment outcome, class enrolment reason for leaving.

Data is derived only from data the active user has permission to see (e.g. if admin, from all data. If a teacher, from those courses they teach, if a student, only from their own data).
