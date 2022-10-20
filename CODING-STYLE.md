## Coding style

Avoid terms that could be considered racist and/or discriminatory

e.g. use:
```
primary, replica, allowlist, blocklist
```

Do not use:
```
master, slave, whitelist, blacklist
```

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
       runAfterRequestingPermissionIfNeeded(WRITE_EXTERNAL_STORAGE) { granted ->
            if(granted) {
                mPresenter?.handleClickDownload()
            }else {
                //Show snackbar that permission is required
            }
       }
   }

}

```


*ARG_ constants that are used with more than one view should be placed on UstadView*


### Entities

Entities are plain Kotlin classes that are used with Room persistence (and on JDBC using lib-door). They must have an empty constructor and a primary constructor in Kotlin.  All number and boolean types must not be nullable. All Strings must be nullable. See [lib-database-entities/README.md](lib-database-entities/README.md) for more details.

### Conventions

#### Spelling

Use US English spellings, the same as system libraries etc.

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

# MVVM

Passing items:

```
//On Android - use ActivityViewModel
class MessageBusViewModel {
     
     val pendingFlows: Map<String, MutableList<Flow<Any?>>
     
     send(key, value) {
         pendingFlows.getOrPut(key) += flowOf {  value }
     }
     
     receive<T>(key): Flow<T> {
        return pendingFlows[key].merge()
     }
}
```

Editing back/forth:
```
@Composable
fun ClazzEdit(viewModel) {
  ..
  
  var clazzState: Clazz by viewModel.entityState()
  
  Text(onChanged = { newText ->
     viewModel.onEntityUpdate(clazzState.copy(clazzName = newText))
  })
}

EditPresenter?loadKey=Schedule&resultDest=<RESULTVIEWNAME>&resultSteps=1&resultKey

{

   val scheduleState = MutableState<List<Schedule>>(listOf())

   init {
       entityState.emit(savedStateHandle.getOrLoadJson<Schedule>(KEY_ENTITY) {
           messageBus.receive(loadKey) ?: repo.onDbThenRepo { dbToUse ->
               dbToUse.dao.findByUid(savedStateHandle.require(ENTITY_PK))
           }
       })
       
       observeReturnedResult<Type>(KEY) { schedule ->
           val newList = scheduleState.collectLatest()
              .replaceOrAppend { it.scheduleUid == schedule.scheduleUid }
           saveState.saveJson(STATE_SCHEDULES, newList)
           scheduleState.emit(newList)   
       }
   }
   
   
   
   fun onEntityUpdate(entity: T) {
       entityState.emit(entity)
       savedState.postCommit(KEY, entity) 
   }
   
   fun navigateForResult() {
       
   }
   	
   fun finishWithResult() {
       //1. put the result onto backstack saved state handle as before...
       
       //2. also put this on the message bus
       
       //3. run the navigation back
   }
      
}   

```
