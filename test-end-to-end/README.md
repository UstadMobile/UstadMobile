# End-to-end tests

End-to-end tests that start a blank new server/app and test functionality end-to-end.

* [android-maestro](android-maestro/) Android end-to-end tests built using [Maestro](https://maestro.mobile.dev)
* [webapp-cypress](webapp-cypress/) Web end-to-end tests built using [Cypress](https://www.cypress.io/)


## Simulating limited connections

It is useful to simulate a lower speed connection to test the app under conditions that more closely
match actual network conditions. This can be done on using trickle:

Run the trickle command (rates are in KB/s) before the startserver.sh command e.g.

```
trickle -s -d 128 -u 128 bash
./startserver.sh ...
```

## Scenarios

### 1 : Library

1.1 [Admin uploads valid content](test-descriptions/001_001_admin_can_add_content_test_description.md)

1.2 [Admin uploads invalid content](test-descriptions/001_002_admin_add_invalid_content_gets_error_message_test_description)

1.3 a) [Admin can delete content and restore it later](test-descriptions/001_003_admin_can_delete_and_restore_content_001_test_description.md)
    b) [Admin can permanently delete content](test-descriptions/001_003_admin_can_permenantly_delete_the_content_002_test_description.md)

1.4 [Admin can move content](test-descriptions/001_004_admin_can_move_content_test_description.md)

1.5 [Admin can remove content and download it again](test-descriptions/001_005_admin_can_remove_content_and_download_again_test_description.md)(Android only).

1.6 [Admin can cancel content download and download it again](test-descriptions/001_006_admin_can_cancel_content_download_test_description.md)

1.7 If a download is started on WiFi and WiFi is stopped midway, the download will stop and then 
    resume when WiFi is resumed. (Android only).

1.8 H5P and video content can be automatically compressed using high, medium, and low quality presets. 

1.9 Admin can delete content from library. Deleting content will remove the files associated with the content from the server. Client devices will recognize any files that are associated with deleted content as eligible for eviction from the cache.

1.10 User can select (in settings) to store content on device storage or memory card (if available - Android apps can only use storage on memory cards that are reformatted on the device to use as storage (not FAT32 etc)). Any subsequent download is saved on the selected storage medium. (Android only).

### 2: Course creation, enrolment, attendance, progress

2.1 [Admin add new course and new members to that course](test-descriptions%2F002_001_admin_add_new_course_and_teacher_test_description.md)

2.2 [Students can join class using class code or link](test-descriptions/002_002_student_joining_course_using_code_test_description.md)

2.3 [Teacher can record attendance.](test-descriptions/002_003_teacher_record_attendance_test_description.md)

2.4 Teacher can view progress report showing each students' result for each block in the course.

2.5 [The course title is mandatory](test-descriptions/002_005_course_title_is_mandatory_test_description.md)

2.6 Teacher/admin can remove people from a course (e.g. delete their enrolment). This removes any permissions that were associated with their enrolment.    

2.7 [Teacher and admin user can grant permission for the course to other users](test-descriptions/002_007_admin_grant_permissions_test_description.md)

2.8 Teacher/admin can add banner photo to the course that is displayed in the course list screen and course detail screen.

2.9 [The user can add a profile photo](test-descriptions/002_009_people_add_and_remove_profile_pic_test_description.md)

### 3: Course block editing

3.1 [Admin or teacher can edit the course](test-descriptions/003_001_add_or_edit_course_permission_test_description.md)

3.2 [Admin or teacher can add module and text blocks](test-descriptions/003_002_add_module_text_blocks_and_perform_indent_hide_delete_actions_test_description.md)

3.3 [Admin or teacher can add a content block and link existing content](test-descriptions/003_003_add_existing_content_in_library_as_block_test_description.md)

3.4 Admin or teacher can add a content block and upload new content via link

3.5 [Admin or teacher can add a content block and upload new content via file upload](test-descriptions/003_005_add_new_content_block_inside_course_test_description.md)

3.6 [Course Modules can be expand/collapse](test-descriptions/003_006_course_view_and_modules_can_expand_collapse_test_description.md)

3.7 [Text blocks can open and all text is visible](test-descriptions/003_007_all_user_able_to_open_text_block_test_description.md)

3.8 [The course block title is mandatory.d](test-descriptions/003_008_course_blocks_title_field_is_mandatory_test_description.md)


### 4: Assignments

4.1 [ssignment creation,submission and grading](test-descriptions/004_001_assignment_creation_submission_grading_test_description.md)

4.2 [Teachers and students can add course comments which are visible for all who can view the assignment](test-descriptions/004_002_users_add_course_comments_test_description.md)

4.3 [Teachers and students can submit and view private comments](test-descriptions/004_003_user_add_private_comment_test_description.md)

4.4 [Assignment is set to allow multiple submissions](test-descriptions/004_004_multiple_submission_possible_for_student_test_description.md)

4.5 [Assignment is submitted after the deadline but before the grace period, the submission should be accepted](test-descriptions/004_005_assignment_after_deadline_and_before_grace_period_test_description.md)

4.6 [Assignment submission page is open after the deadline the submit button won't be visible](test-descriptions/004_006_assignment_grace_period_finished_test_description.md)

4.7 [Assignment submission page is open before the deadline, and the deadline passes whilst the screen is open, the student will not be able to submit](test-descriptions/004_007_user_enter_assignment_page_before_graceperiod_but_submission_after_grace_Period_test_description.md)

4.8 [Group users can add assignment and course comments](test-descriptions/004_008_group_users_add_assignment_and_course_comments_test_description.md)

4.9 [Group users can add assignment and private comments](test-descriptions/004_009_group_users_add_private_comments_test_description.md)

4.10 [Group assignment is set that only one submission is allowed](test-descriptions/004_010_group_users_single_assignment_submission_allowed_test_description.md)

4.11 [Group assignment is set to allow multiple submission](test-descriptions/004_011_group_users_multiple_assignment_submission_allowed_test_description.md)

4.12 [Assignment is set to be marked by peers and submitted by individual users](test-descriptions/004_012_peer_marking_for_individual_assignment_test_description.md)

4.13 [Assignment is set to be marked by peers and submitted by groups](test-descriptions/004_013_peer_marking_for_group_assignment_test_description.md)

4.14 Teacher/admin (any user with permission to edit the course itself) can delete private comments and course comments.

4.15 Students may upload attachments for assignment submissions if enabled by the teacher, up to the maximum file size limit set by the teacher. 

### 5	Discussion board

5.1 [Teacher can add discussion board to course](test-descriptions/005_001_add_discussionBoard_test_description.md)

5.2 [Teacher can edit discussion board](test-descriptions/005_002_teacher_can_edit_discussion_board_test_description.md)

5.3 [All users on course can post on discussion board](test-descriptions/005_003_users_can_add_post_on_discussion_board_test_description.md)

5.4 [Users can include internal links in their post](test-descriptions%2F005_004_user_add_internal_links_to_post_test_description.md) 

5.5 [Users can include external links to websites in their post](test-descriptions/005_005_users_can_add_external_links_as_post_on_discussion_board_test_description.md)

5.6 [All users with permission can delete posts and replies](test-descriptions/005_006_users_can_delete_post_on_discussion_board_test_description.md)

### 6 Messaging

6.1 Users can send a chat message (plain text) to any other user that they have permission to see (
    teachers and students can see those who are part of their courses by default). The recipient can
    reply to the message. Messages/replies are delivered instantly when the app is open. Note: 
    Messages are only delivered if the app is open and notifications are not displayed.

6.2 When a user receives a chat message from a user that they could not normally see (e.g. when the
    admin sends them a message), they will be able to see the name of the sender.

	
### 7	User accounts:

7.1 [Admin can enable/disable registration for users](test-descriptions/007_001_admin_enable_or_disable_user_registration_test_description.md)

7.2 [When registration is enabled: Users over 13 can register directly](test-descriptions/007_002_user_registration_above_age_13_test_description.md)

7.3 When registration is enabled: Users under 13 can register using parental approval link

7.4 [For user registration date of birth field is mandatory](test-descriptions/007_004_user_registration_dob_field_is_mandatory_test_description.md)

7.5 [Students user under 13 is added by a teacher/admin, don't require parental approval](test-descriptions/007_005_student_registered_by_admin_or_teacher_dont_need_parentConsent_test_description.md)

7.6 [Admin can enable/disable guest login](test-descriptions/007_006_admin_enable_or_disable_guest_login_test_description.md)

7.7 Teacher (course leader) or admin can send an invitation to a list of users by providing phone 
    numbers (SMS) or email addresses. User can open the invitation, create a new account if this is 
    allowed by admin policy and they don't have an existing account, and join a course.

7.8 [On user registration page first name, last name, gender, username and password are mandatory fields](test-descriptions/007_008_user_registration_mandatory_fields_test_description.md)

7.9 [For user registration email id field should have valid email address](test-descriptions/007_009_user_registration_email_field_verification_test_description.md)

7.10 [For user registration phone number field should have valid phone number](test-descriptions/007_010_user_registration_phone_field_verification_test_description.md)

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

9.1 If a user is connected to the same network (e.g. LAN) as another user they can request to use the other device as a proxy. If the other user approves, they will be able to login and access and edit their data the same as if they were connected directly to the server provided that a) the other device already has the relevant data or b) the other device has an Internet connection

9.2 If a user is connected to the same network as another user and downloads content (e.g. supported content types such as H5P, EPUB, Video, as above) and another device on the smae network has those content files, then the content will be downloaded from the other device instead of from the Internet.

9.3 A user on an Android device may create a new mesh hotspot (e.g. if there is not an existing WiFi access point). Users on other devices may join the hotspot by scanning a QR code. Devices can simultaneously provide a hotspot for other devices and connect to one other hotspot on Android versions that support this (Android 10+ that supports IPv6 and WiFi direct, which includes almost all Android 10+ devices, or those that support Wifi Station/Access point concurrency which is limited to those that have chipset support on Android 11+). All devices connected can communicate with each other (e.g. over multiple hops).
    
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

### 14 Offline behavior (Android and Desktop only)

14.1. If the user visits a screen whilst online, and then returns whilst offline, the screen will load using cached data.

14.2. If the user edits data offline, the data will be sent to the server as soon as the app is open and a connection is available.

14.3 If the same piece of data was edited by two users offline simultaneously, the edit that was performed most recently will take effect.

14.4 If a user visits a screen whilst offline that cannot be loaded (because the data is not available), an error message will be displayed.

