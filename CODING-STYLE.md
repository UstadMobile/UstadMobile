## Coding style

The app fundamentally follows a Model-View-Presenter (MVP) design. E.g.

```
class BaseNamePresenter: UstadPresenter

     fun onCreate(savedState: Map<String,String>)

     fun handleUserClickedButton()

     fun handleUserClickedEntity(entity: BaseEntity)

```

**Never, ever, shall thy ever use !! in production Kotlin code.** The !! operator is OK in unit tests, but should never be used in production code.

e.g. use:

```
val someEntityName = someEntity?.name
if(someEntityName != null) {
    //smart cast
}
```
Do not do this:
```
if(someEntity?.name != null) {
    println(someEntity!!.name!!)
}
```

use:
```
memberVar = SomeEntity().apply {
   someField = "aValue"
}
```

Do not do this:
```
memberVar = SomeEntity()
memberVar!!.someField = "aValue"
```


### Presenters

The BaseName should be suffixed with List, Detail, or Edit to describe it's function:

*Name*List, *Name*Detail, *Name*Edit for list, detail, and edit screens e.g. *ContentEntry*List, *ContentEntry*Detail, *ContentEntry*Edit

For each screen there should be the following classes:

* *BaseName*Presenter - the core Kotlin multiplatform presenter
* *BaseName*View - the view interface
* *BaseName*(Activity|Fragment) - the Android implementation of the view
* *base-name*.component.ts - the Angular implementation of the view


### Views

Views should **not** contain any business logic. They simply display information. They are an interface and should have methods like:

```
interface BaseNameView: UstadView

   var BaseName: BaseEntity

   var someButtonVisible: Boolean

   companion object {
        const val VIEW_NAME = "BaseName"

        const val ARG_SOMEPARAM = "someParam"
   }

```

Displaying the properties of an entity is handled by data binding on Android and by Angulars own template pattern.
Do not create a variable for each property (e.g. title, author, etc). Just pass the entity itself
and use data binding.

If handling an event would require permission (e.g. download requires file permission), the native
element (eg. fragment) should check for permission before calling the view method.

e.g.
```
class SomeDetailFragment {

   var mPresenter: SomeDetailPresenter? = null

   ...

   fun handleClickDownload() {
       runAfterPermissionGranted(WRITE_EXTERNAL_STORAGE) {
            mPresenter?.handleClickDownload()
       }
   }

}

```


*ARG_ constants that are used with more than one view should be placed on UstadView*


### Entities

Entities are plain Kotlin classes that are used with Room persistence (and on JDBC using lib-door). They must have an empty constructor and a primary constructor in Kotlin.  All number and boolean types must not be nullable. All Strings must be nullable. See [lib-database-entities/README.md](lib-database-entities/README.md) for more details.

### Conventions

#### runOnUiThread

It is the job of the presenter to call runOnUiThread when needed. *DO NOT* put runOnUiThread in the view itself.

#### Tab lists

The presenter should build a list of subviews as a String list. This can be a list containing only the VIEW_NAME of each tab to be displayed, or it can contain arguments as a query string. The name of the tab might be fixed (e.g. an instance of VIEW_NAME always has the same tab name, in which case a constant map can be used) or it might be needed to build this into the string. e.g. ["Tab1?arg1=value1", "Tab12arg1=value1"] or ["TabTitleMessageID;Tab1?arg1=value1", "TabTitleMessageID;Tab2?arg1=value1"]

#### Localization strings

The name for the string should be just the string itself. Only add extra text if the translation would be different due to a different context.

use:
```
<string name="download">Download</string>
```

Do not use this:
```
<string name="myscreen_download">Download</string>
```

Where some context might be needed to translate this make sure to put a comment before:
e.g.
```
<!-- Used to set the title on an edit screen where the user is creating a new entity e.g.
new class, new assignment, etc. %1$s will be replaced with the name of the item.-->
<string name="new_entity">New %1$s</string>
```

