### Pull Request Checklist

#### Coding style

* Ensure all code follows the [Coding Style](CODING-STYLE.md)

* Ensure that all new variables and functions have **meaningful** names. The name should make the purpose clear.

For example, if you want to have a variable that controls the visibility of a group of widgets for
that handle resetting a password:
Good:
```
var passwordResetVisible = false
```

Not good (visibility and enabled are different - a disabled field can be visible):
```
var passwordResetEnabled = false
```

Not good (unspecific)
```
var fieldsVisible = false
```

* Ensure that logic and display are not mixed. Business logic should be in the ViewModel and/or domain UseCase, and the view
should display it. The ViewModel should tell the view what to display, and receive events

* Commented out code is not acceptable. Git is used to track revisions. Unused code must be deleted.

* If adding any third-party assets via any system other than Gradle dependencies you must check to ensure it is available under a compatible open license. If the license is not contained in the file itself, you must place a text file in the same directory in the source code with a link to its original URL and the license under which it is used.

* If new strings are added you must screenshot the string as it is used within the app and save the screenshot into translate-screenshots where the filename must be ```string_name.png``` (string_name as per name of the string in the XML file).
  
* There must be no hardcoded strings that should be variables. Strings may be hardcoded if they will never change e.g. when path segments based on a API specificiation. They must not be hardcoded where they might change (e.g. server endpoints etc).

#### Automated tests

* If the same text is clicked twice (or more) in a row (e.g. clicking done twice), then you must use a check/wait condition to ensure that the screen has changed following the first click. This avoids accidentally clicking twice on the same button.

* The test must not depend on any external websites or services which, if they fail, would cause the test to (incorrectly) fail.
  
* No use of automatically generated selectors (e.g. automatically generated css classes)
  
* Selectors must be consistent and predicatable e.g. must not use index:0 unless we can be certain that an item will always be first in the list

### Testing

* Ensure all screens have unit tests and end-to-end tests. All tests must run to completion and pass.

* Ensure that the test assertion validates what is being tested. E.g. if a particular result is
expected, do not just use assertNotNull. Check the actual result.

* Ensure that test method name follows the givenCondition_whenSomethingHappens_thenResult format.

* Ensure that tests cover all key relevant scenarios. You don't need to add another test if there is
no logic change (e.g. if an entity edit field has multiple fields (e.g. author, publisher, etc), and
those fields have no impact on the logic, you don't need individual tests for each one being filled.
If those fields have an impact on the logic (e.g. title is a required field, and the form should not
be saved if the title is empty), then this does require an extra test scenario.

