
### Architecture

The project uses Kotlin Multiplatform to provide an Android App (using Kotlin / Jetpack compose),
web client app (using Kotlin/JS wrappers for React and MUI), and an http server backend (using KTOR). 
Door (Room for Kotlin Multiplatform) is used to provide a Kotlin Multiplatform database and sync.

The app follows an MVVM pattern as follows:

* UIState classes contain all information needed to show a screen. This often includes entity objects
* ViewModels contain the logic and emit a flow of the UIState class.
* Views observe the UIState flow to render the user interface (using Jetpack Compose or React/JS)

### UiState classes

The UiState class contains everything needed to render a screen. It is emitted as a flow from the
ViewModel. It is a data class contained in the same file as the ViewModel.

When converting an existing MVP screen, the ViewModel should generally have all the same properties that
are found on the existing view interface (but properties should be non-mutable val instead of var).

The UiState class should also contain the model entity. It should use the same model type as found
on the view (e.g. because PersonDetailView uses PersonWithPersonParentJoin, PersonDetailUiState 
should contain PersonWithPersonParentJoin).

```
data class PersonDetailUiState(
    val person: PersonWithPersonParentJoin? = null,

    val changePasswordVisible: Boolean = false,

    val showCreateAccountVisible: Boolean = false,

    val chatVisible: Boolean = false,

    val clazzes: List<ClazzEnrolmentWithClazzAndAttendance> = emptyList(),
) {
    
    //Where view information is derived from other parts of the state, use a simple getter e.g.
    val emailAddressVisible: Boolean
        get() = !person?.emailAddress.isNullOrEmpty()
}
```

### ViewModels

The ViewModel is responsible for all business logic. It emits a flow of the UiState class, which is
observed and rendered by the view (e.g. via Jetpack Compose and React/JS). It has event handling
functions that can be called by the view when events take place (e.g. when a user clicks a button).

The BaseName should be suffixed with List, Detail, or Edit to describe it's function:

*Name*ListViewModel, *Name*DetailViewModel, *Name*EditViewModel for list, detail, and edit screens
e.g. *ContentEntry*ListViewModel, *ContentEntry*DetailViewModel, *ContentEntry*EditViewModel.

**Kotlin multiplatform implementation**: There is a base abstract ViewModel class in common that uses expect/actual. This is a child class of 
the Android ViewModel itself on Android. There is a minimal implementation that creates and destroys
the Coroutinescope for Javascript.

e.g.

```
class PersonDetailViewModel: ViewModel {
    val uiState: Flow<PersonDetailUiState>
    
    init {
        //Logic to seutp the uiState here
    }
    
    //Event handlers here
    fun onClickCreateAccount() {
    
    }
    
    fun onClickChat() {
    
    }
    
    fun onClickClazz(clazz: ClazzEnrolmentWithClazzAndAttendance) {

    }
}

```

### Views

Views are written using Jetpack Compose for Android and the Kotlin/JS MUI wrapper for Javascript. 
The view function should use the UiState as an argument, 

Android Jetpack Compose:
```
/*
 * Main composable function: this should always take the UI state as the first parameter, and then
 * have parameters for event handlers.
 */ 
@Composable
function PersonDetailScreen(
    uiState: PersonDetailUiState = PersonDetailUiState(),
    onClickCreateAccount: () -> Unit,
    onClickChat: () -> Unit,
    onClickClazz: (ClazzEnrolmentWithClazzAndAttendance) -> Unit,
) {
    //UI functions go here eg.
    Row {
        if(uiState.chatVisible) {
            Button(onClick = onClickChat) {
                Text(stringResource(R.id.chat))
            }
        }
        
        //Use the object on the UiState to show properties
        Text(uiState.person?.firstNames + uiState.person?.lastName)
       
        Text(uiState.person?.phoneNumber)
    }
}

/*
 * Note: different function name is used to avoid rendering issues in Android studio if using
 * the same function name with different parameters (which is valid and will compile, but then the 
 * the preview in Android Studio sometimes won't work).
 */
@Composable
function PersonDetailScreenForViewModel(
    viewModel: PersonDetailViewModel
) {
    val uiState: PersonDetailUiState by viewModel.uiState.collectAsState(initial = null)
    
    //Always use named arguments here e.g. onClickChat to avoid potential mismatch.
    PersonDetailScreen(
        uiState = uiState, 
        onClickChat = viewModel::onClickChat,
        onClickClazz = viewModel::onClickClazz, 
        onClickCreateAccount = viewModel::onClickCreateAccount
    )
}

@Composable
@Preview
function PersonDetailScreenPreview(
    uiState = PersonDetailUiState(
         person = Person().apply {
              firstNames = "Preview"
              lastName = "Person"
         }
    )
)
```


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

**Never, ever, shall thy ever use !! in production Kotlin code.** The !! operator is OK in unit tests, 
but should never be used in non-test code.

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

### Conventions

#### Spelling

Use US English spellings, the same as system libraries etc.

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




*Below is work-in-progress...*


# MVVM thoughts, ideas, drafts, etc.

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


Handling App Ui State (e.g. fab, snackbar, etc.)

1. ViewModel emits the AppUiState
```

class AppUiState {
    val fabText: String,
    val onClickFab: () -> Unit,
}

UstadViewModel( ... ) {
    protected val _appUiState = MutableStateFlow(AppUiState())

    val appUiState: Flow<AppUiState>
        get = _appUiState.asStateFlow()
    
}
```

2. State is collected 
 a. By Fragment: collected and then sent to the activity as needed
 b. In Jetpack compose:
 ```
 App.kt
 fun App() {
     
     var appUiState: AppUiState by rememberSaveable { mutableStateOf(AppUiState) }
     
     SomeScreen(
         viewModel,
         onAppStateChange = { appUiState = it }
     )
     
     Fab {
         Text()
     }            
 }
 
 
 SomeScreen.kt:
 fun SomeScreen(
     entity: SomeEntity,
 ) {
     Text(...)
 }
 
 fun SomeScreen(
      viewModel: ViewModel,
      onAppStateChange: (AppUiState) -> Unit,
 ) {
     LaunchedEffect(Lifecycle) {
         viewModel.appUiState.collectWithLifecycle {
            appUiState = it
         }
     }
     
     val someUiState: SomeUiState by viewModel.uiState.collectAsState()
     
     val entity: SomeEntity by someUiState.entity.collectAsState()
     
     SomeScreen(entity)
 }
```

c. By React Functional component

```
val SomeScreen = FC<AppProps>() {
    val viewModel: SomeViewModel by useViewModel()
    
    //Or: could use Redux to dispatch this if that's the way for JS
    // See: https://github.com/Kotlin/react-redux-js-ir-todo-list-sample/tree/master/src/main/kotlin/reactredux
    
    useEffect(dependencies = "viewModel") {
        viewModel.appUiState.collectLatest {
            appProps.onAppStateChange(it)
        }
    }
}

```
