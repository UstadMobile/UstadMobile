# Admin can enable/disable registration for users and manage visibility of buttons in bottom navigation bar

## Description:

Admin can enable/disable registration for users and manage visibility of buttons in bottom navigation bar

## Step-by-Step Procedure:

* **1.Admin Enables Registration and Sets Navigation**
* Launch Ustad app
* Tap Existing User
* Tap Enter link manually
* Enter the Site link
* Tap Next
* Login using Admin credentials
* Tap Settings → Site
* Confirm Navigation bar is visible
* Tap Edit (floating action button)
* Enable Registration Allowed
* Tap Save
* Verify warning:
* “If self-registration is enabled, you must set terms and policies…”
* Reopen Edit, disable and re-enable Registration Allowed
* Tap Terms and policies (English)
* Enter text: "Terms and company policies"
* Tap Done
* Tap to disable Messages and People
* Tap Save
* Take a screenshot of enabled state
* Verify bottom navigation shows Courses, Library
* Confirm Messages, People are hidden
* **2. New User Registration Flow (Mandatory Field Validation)**
* Clear app storage
* Open app
* Tap New User
* Tap Join Learning Space
* Tap Enter link manually
* Enter Site link
* Tap Next
* Tap Next without DOB
* Verify error: "This field is required."
* Select DOB (2009 or earlier)
* Tap Next
* Verify "Terms and company policies" is shown
* Tap Accept
* Tap Other options
* Verify errors under Full Name and Gender fields
* Enter Full Name: User ABC
* Select Gender: Female
* Tap Other options
* Tap Create username and password
* Tap Sign-up
* Verify error under Password
* Enter Password: test1234
* Tap Sign-up
* User logs in successfully
* Verify bottom navigation shows Courses and Library
* Verify Messages, People are hidden
* **3. Admin Disables Registration and Restricts Navigation**
* Clear app storage
* Open app
* Tap Existing User
* Tap Enter link manually
* Enter Site link
* Tap Next
* Login using Admin credentials
* Tap Library
* Tap Settings → Site
* Tap Edit
* Disable Registration Allowed
* Disable Courses and Library
* Tap Save
* Verify error: "Please enable at least one navigation option."
* Re-enable Library
* Tap Save
* Verify only Library is visible
* Confirm Courses, Messages, People are hidden
* **4. New User Registration Blocked**
* Clear app storage
* Open app
* Tap New User
* Tap Join Learning Space
* Tap Enter link manually
* Enter Site link
* Tap Next
* Verify message: "Learning space does not allow new user registration."
* **5. New User View (Library Only)**
* Clear app storage
* Open app
* Tap Existing User
* Tap Enter link manually
* Enter Site link
* Tap Next
* Login with:
* Username: newuser
* Password: test1234
* Verify only Library is visible
* Confirm Courses, Messages, People are hidden