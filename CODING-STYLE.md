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

Displaying the properties of an entity is handled by data binding on Android and by Angulars own template pattern. It is not required to have a getter/setter for each property.

*ARG_ constants that are used with more than one view should be placed on UstadView*


### Entities

Entities are plain Kotlin classes that are used with Room persistence (and on JDBC using lib-door). They must have an empty constructor and a primary constructor in Kotlin.  All number and boolean types must not be nullable. All Strings must be nullable. See [lib-database-entities/README.md](lib-database-entities/README.md) for more details.

### Conventions

#### runOnUiThread

It is the job of the presenter to call runOnUiThread when needed. *DO NOT* put runOnUiThread in the view itself.

#### Tab lists

The presenter should build a list of subviews as a String list. This can be a list containing only the VIEW_NAME of each tab to be displayed, or it can contain arguments as a query string. The name of the tab might be fixed (e.g. an instance of VIEW_NAME always has the same tab name, in which case a constant map can be used) or it might be needed to build this into the string. e.g. ["Tab1?arg1=value1", "Tab12arg1=value1"] or ["TabTitleMessageID;Tab1?arg1=value1", "TabTitleMessageID;Tab2?arg1=value1"]



