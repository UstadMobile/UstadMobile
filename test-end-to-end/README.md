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

1.5 [Content download is started on WiFi,the download will stop when wifi off and then resume when WiFi is resumed.](test-descriptions%2F001_005_content_download_will_stop_when_wifi_stop_then_resume_when_WiFi_is_resumed%20_test_description.md)

1.6 [Admin can cancel content download and download it again](test-descriptions/001_006_admin_can_cancel_content_download_test_description.md)

1.7 [H5P and Video Content Compression](test-descriptions/001_007_h5p_and_video_content_compression_test_description.md)

1.8 [Deleting content will remove the files associated with the content from the server](test-descriptions/001_008_deleting_content_will_remove_the_files_associated_with_the_content_from_the_serve%20_test_description.md)

1.9 [User can store offline content on device storage or memory card](test-descriptions/001_009_user_can_store_offline_content_on_device_storage_or_memory_card_test_description.md)

### 2: Course creation, enrolment, attendance, progress

2.1 [Admin add new course and new members to that course](test-descriptions%2F002_001_admin_add_new_course_and_teacher_test_description.md)

2.2 [Students can join class using class code or link](test-descriptions/002_002_student_joining_course_using_code_test_description.md)

2.3 [Teacher can record attendance.](test-descriptions/002_003_teacher_record_attendance_test_description.md)

2.4 [Teacher can view progress report showing each students' result in Gradebook](test-descriptions/002_004_teacher_can_view_progress_report_showing_each_students_result_in_gradebook_test_description.md)

2.5 [The course title is mandatory](test-descriptions/002_005_course_title_is_mandatory_test_description.md)

2.6 [Teacher/admin can remove people from a course](test-descriptions/002_006_teacher_or_admin_can%20remove_people_from_a_course_test_description.md)

2.7 [Teacher and admin user can grant permission for the course to other users](test-descriptions/002_007_admin_grant_permissions_test_description.md)

2.8 [Teacher/admin can add banner photo to the course](test-descriptions/002_008_teacher_or_admin_can_add_banner_photo_to_the_course_test_description.md)

2.9 [The user can add a profile photo](test-descriptions/002_009_people_add_and_remove_profile_pic_test_description.md)

### 3: Course block editing

3.1 [Admin or teacher can edit the course](test-descriptions/003_001_add_or_edit_course_permission_test_description.md)

3.2 [Admin or teacher can add module and text blocks](test-descriptions/003_002_add_module_text_blocks_and_perform_indent_hide_delete_actions_test_description.md)

3.3 [Admin or teacher can add a content block and link existing content](test-descriptions/003_003_add_existing_content_in_library_as_block_test_description.md)

3.4 [Admin or teacher can add a content block and upload new content via link](test-descriptions/003_004_admin_or_teacher_can_add_a_content_block_and_upload_new_content_via_link_test_description.md)

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

4.14 [Teacher or admin can delete private comments and course comments](test-descriptions/004_014_teacher_or_admin_can_delete_private_comments_and_course_comments_test_description.md)

4.15 [Admin or teacher can enable or disable the file submission and can limit maximum file size](test-descriptions/004_015_teacher_or_admin_enable_or_disable_file_submission_test_description.md)

### 5	Discussion board

5.1 [Teacher can add discussion board to course](test-descriptions/005_001_add_discussionBoard_test_description.md)

5.2 [Teacher can edit discussion board](test-descriptions/005_002_teacher_can_edit_discussion_board_test_description.md)

5.3 [All users on course can post on discussion board](test-descriptions/005_003_users_can_add_post_on_discussion_board_test_description.md)

5.4 [Users can include internal links in their post](test-descriptions%2F005_004_user_add_internal_links_to_post_test_description.md) 

5.5 [Users can include external links to websites in their post](test-descriptions/005_005_users_can_add_external_links_as_post_on_discussion_board_test_description.md)

5.6 [All users with permission can delete posts and replies](test-descriptions/005_006_users_can_delete_post_on_discussion_board_test_description.md)

### 6 Messaging

6.1 [Users on same course can send a chat message](test-descriptions/006_001_users_on_same_course_can_send_a_chat_message_test_description.md)

6.2 [Admin user can send a chat message to students](test-descriptions/006_002_admin_user_can_send_a_chat_message_to_students_test_description.md)
	
### 7	User accounts:

7.1 [Admin can enable/disable registration for users](test-descriptions/007_001_admin_enable_or_disable_user_registration_test_description.md)

7.2a [When registration is enabled: Users over 13 can register directly](test-descriptions/007_002a_user_registration_above_age_13_test_description.md)

7.3 [When registration is enabled: Users under 13 can register using parental approval link](test-descriptions/007_003_users_under_13_can_register_using_parental_approval_link_test_description.md)

7.4 [For user registration date of birth field is mandatory](test-descriptions/007_004_user_registration_dob_field_is_mandatory_test_description.md)

7.5 [Students user under 13 is added by a teacher/admin, don't require parental approval](test-descriptions/007_005_student_registered_by_admin_or_teacher_dont_need_parentConsent_test_description.md)

7.6 [Admin can enable/disable guest login](test-descriptions/007_006_admin_enable_or_disable_guest_login_test_description.md)

7.7 [Admin or teacher send invitation via message or phone numbers or email addresses](test-descriptions/007_007_admin_or_teacher_send_group_invitation_to_join_course_test_description.md)

7.8 [On user registration page first name, last name, gender, username and password are mandatory fields](test-descriptions/007_008_user_registration_mandatory_fields_test_description.md)

7.9 [For user registration email id field should have valid email address](test-descriptions/007_009_user_registration_email_field_verification_test_description.md)

7.10 [For user registration phone number field should have valid phone number](test-descriptions/007_010_user_registration_phone_field_verification_test_description.md)

### 8 Accessibility

8.1 [Admin uploads valid content with video subtitles](test-descriptions/008_001_admin_uploads_valid_content_with_video_subtitles_test_description.md)

8.2 [Accessibility: Text-to-Speech and Image Button Labels](test-descriptions/008_002_accessibility_text-to-Speech_image_button_labels_test_description.md)

### 9 Administration

9.1 [Server can be installed via script.](test-descriptions/009_001_server_can_be_installed_via_script._test_description.md)

### 10 Language support

10.1 [Default system language is used on app if the language is supported by the app](test-descriptions/010_001_default_system_language_if_language_is_supported_by_app_test_description.md)

10.2 [Default English language is used on app if the language is not supported by the app](test-descriptions/010_002_english_language_used_if_system_language_not_supported_by_the_app_test_description.md)

10.3 [Language Selection in App Settings](test-descriptions/010_003_language_selection_in_app_settings_test_description.md)

10.4 [Android 13+ users can select language in app settings](test-descriptions/010_004_android_13%2B_users_can_select_language_in_app_settings_test_description.md)

### 11 Offline behavior (Android and Desktop only)

11.1. [Screen Caching for Offline Access](test-descriptions/011_001_screen_caching_for_offline_access_test_description.md)

11.2. [Offline Data Editing and Syncing](test-descriptions/011_002_Offline_data_editing_and_syncing_test_description.md)

11.3 [Handling conflicting offline edits](test-descriptions/011_003_handling_conflicting_offline_edits_test_description.md)

11.4 [User visits a screen whilst offline that cannot be loaded gets an error](test-descriptions/011_004_user_visits_a_screen_whilst_offline_that_cannot_be_loaded_gets_an_error_test_description.md)
