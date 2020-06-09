### Pull Request Checklist

#### Coding style

* Ensure all classes follow the [Coding Style](CODING-STYLE.md) ? Ensure all screens and classes named
according to the pattern (e.g. FooDetailPresenter, FooDetailFragment)?

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

* Ensure that logic and display are not mixed. Business logic should be in the presenter, and the view
should display it. The presenter should tell the view what to do, and receive events. The view
generally **should not** otherwise talk back to the presenter. An edit view should use two-way data
binding to save the result of the changes made by the user to the entity object.

Good:
```
Presenter:
   view.entity = queryResult

Fragment:
   mBinding?.entity = value

View XML:
   <TextView
   ...
   android:customBinder="@{entity.value}/>
```

Not good:
```
Presenter:
   view.entity = queryResult

Fragment:
   mBinding?.entity = value

View XML:
   <TextView
   ...
   android:text="@{mPresenter.formatSomething(entity.value)}/>
```


### Testing

* Ensure all screens have JVM and Espresso tests as per relevant templates. All tests should run to
completion and pass.

* Ensure that the test assertion validates what is being tested. E.g. if a particular result is
expected, do not just use assertNotNull. Check the actual result.

* Ensure that test method name follows the givenCondition_whenSomethingHappens_thenResult format.

* Ensure that tests cover all key relevant scenarios. You don't need to add another test if there is
no logic change (e.g. if an entity edit field has multiple fields (e.g. author, publisher, etc), and
those fields have no impact on the logic, you don't need individual tests for each one being filled.
If those fields have an impact on the logic (e.g. title is a required field, and the form should not
be saved if the title is empty), then this does require an extra test scenario.

### Leaks

* Ensure that there are **no memory leaks detected by Leak Canary.**. You need to navigate to each
screen and wait for at least 5 seconds. If Leak Canary "sings", then ensure the leak is removed. Make
sure to implement onDestroyView in all fragments. Make sure that all RecyclerViewAdapters are detached
and any reference to the presenter, fragment, or context are set to null. See the UstadListViewFragment
for a reference on dealing with RecyclerViews


