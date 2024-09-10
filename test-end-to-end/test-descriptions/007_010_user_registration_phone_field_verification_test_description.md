# For user registration phone number field should have valid phone number

## Description:

If the phone number is not blank, it must contain a valid phone number. Phone number validation will be performed by libphonenumber which understands location-specific rules on number length etc.

## Step-by-Step Procedure:

1. Login as admin.
2. Click on "Settings" icon.
3. Click on "Site" button.
4. Click on "Edit" button.
5. Enter Company terms in "Terms and policies" text field.
6. Click on "Registration allowed" switch.
7. Click on "Save" button
8. Clear app storage.
9. Click on "Create account" button.
10. Select date of birth , age 13+
11. Click on the "Next" button.
12. Click "Accept" button in Terms and policies page.
13. Enter First name, Last name, Gender.
14. Enter invalid phone number with 5 digits in Phone number field.
15. Enter username and password
16. Click on "Register" button.
17. Verify user gets an error just below phone number as "Invalid"
18. Clear the phone number field.
19. Enter valid phone number in the phone number field.
20. Click on "Register" button.
21. Verify the user logged into the app.