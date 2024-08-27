
# Coding pattern

The app follows an MVVM pattern as follows:
* View layer is built using:
  * [UiState](#uistate-classes) classes contain all information needed to show a screen e.g. 
  ```data class PersonDetailUiState(..)```. This often includes entity objects from the data layer.
  * [ViewModels](#viewmodels) (e.g. ```class PersonDetailViewModel```) contain the logic and event handlers for the screen 
   (e.g. onClickButton etc) and emit a flow of the UIState class.
  * [Screens](#screens) (e.g. ```fun PersonDetailScreen(viewModel)``` observes the UIState flow from the ViewModel 
  to render the user interface (using a Jetpack Compose function on Android and Desktop and a React 
  Functional Component using the Kotlin/JS wrapper for the web).
* Domain Layer that contains UseCase(s) as per [Android Achitecture Recommendations](https://developer.android.com/topic/architecture/domain-layer)
  * [UseCase](#usecase): where a single class can work for all platforms then a single class can be added 
   e.g. ```class AddNewPersonUseCase```. Where different implementations are needed for different
   platforms then create an interface e..g. ```interface OpenExternalLinkUseCase```.
* Data layer: this is the database provided using Room and [Door](https://www.github.com/UstadMobile/door/) found in the ```lib-database``` module.

All Kotlin code should follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html). SQL queries should follow
[SQLStyle.guide](https://www.sqlstyle.guide/).

## Use of AI tools

AI generated code is prone to errors, and the code generated often looks like it _should_ be right, but
isn't. AI tools should be used _only when the author is confident that they can inspect the generated
code and spot such mistakes/errors_. All code must still adhere to this coding style and meet the
requirements in [PULLREQUEST-CHECKLIST.md]. Sometimes it will be quicker to spend one hour writing
something manually, rather than spending many more hours trying to debug the code the AI wrote in 
30 seconds.

## View layer

### UiState classes

The UiState class contains everything needed to render a screen. It is emitted as a flow from the
ViewModel. It is a data class contained in the same file as the ViewModel.

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

### Screens

Screens are written using Jetpack Compose for Android and the Kotlin/JS MUI wrapper for Javascript. 
The screen function should use the UiState as an argument, 

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

## Domain Layer

### UseCase

A UseCase will be named as per the Android architecture recommendations in the form of Verb(Noun-optional)UseCase
and will contain a single invoke function. A UseCase can depend on other UseCases which should be provided as 
constructor parameters.

e.g.
```
class DoThingsUseCase(
   private val dependency: OtherUseCase,
) {
    data class WhatToDo(
        val what: String,
        val howMuch: Int,
    )

    data class DoThingsResult(
         val howMuchDone: Int,
         val notDone: List<String>
    )

    suspend operator fun invoke(
        todo: List<WhatToDo>,
        progressListener: (Int) -> Unit,
    ) {
        //.. do stuff here
        return ToDoThingsResult(..)
    }
}
```

Where different implementations are required for different platforms the UseCase itself should be an 
interface (e.g. ```interface DoThingsUseCase```) and then implementations for each platform should be
in the same package (e.g. ```class DoThingsUseCaseJvm```, ```class DoThingsUseCaseAndroid```, 
```class DoThingsUseCaseJs``` etc)

The UseCase should be bound using the dependency injection (KodeIn-DI). If there is only one implementation
it can normally be bound in CommonDomainDi. If there are different implementations for different platforms it
be bound in the modules for each platform (e.g. UstadJsDi on Kotlin/JS, DesktopDomainDiModule on desktop, UstadApp
and AbstractAppActivity on Android). On Andoid any UseCase that needs to use strings MUST be bound in the activity
because per-app locales require the activity context and don't work with the application context.

## Common situations

### Returning values from one screen to another

This can be done by adding arguments to the navigation that indicate what is being picked. Once the 
pick is done, the selected value put on the NavResultReturner via context / navGraphViewModel and
the stack is popped so the user returns to the start view. This works when the user navigates 
directly from one sceren to another, or when the user goes via any number of other screens (e.g.
start screen - pick from list - edit new entity - return to start screen). This is a little bit
similar to startActivityForResult on Android.

#### Scenario 1: User starts on a screen, navigates to pick a value, value is returned to start screen

Sometimes users might navigate from one screen to another to pick an entity, for example:

* Users creates a new course or edits an existing one in ClazzEdit screen
* User clicks to select holiday calendar for course. User arrives at the HolidayCalendarList screen.
* The user might select an existing holiday calendar, or they might create a new one.
* User is returned to ClazzEdit. The selected or newly created HolidayCalendar is now selected for 
  the course.

This is achieved as follows:

Two arguments are passed along as the user moves through screens:
ARG_ON_NAV_RESULT_POP_UP_TO : When a result is 

ARG_RESULT_DEST_VIEWNAME : The destination viewname to which a value will be returned
ARG_RESULT_DEST_KEY : The key (string) that will be used so that the screen which requested a value 
can recognise what kind of value is incoming.

When the value is selected (e.g. at the list screen or in the edit screen if a new item is created):

* The list screen or edit screen recognizes (via the presence of the ags) that a return result is
  expected. It will call navResultReturner.sendResult
* The list screen or edit screen will perform a navigation stack pop to return the user to the screen
  which requested the value (e.g. ARG_RESULT_DEST_VIEWNAME )
* The screen where the result was expected (e.g. the course edit in this case) will observe for values
  via UstadViewModel.collectReturnedResults .

#### Scenario 2: User starts on a screen, navigates to pick a value, value is provide as an argument to another screen, then user is returned to start screen

For example:

* User is viewing the list of students in ClazzDetail. Users selects to add another student
* The user might pick a person from the list, or create a new person
* The selected person uid is sent as an argument to ClazzEnrolmentEdit
* The user saves the enrolment and is returned to ClazzDetail.

The arguments set are as follows:

PersonEditView.ARG_GO_TO_ON_PERSON_SELECT: the screen to which the user will be directed when they
select a person (PersonEdit and PersonList recognise this, and it takes precedence over the presence
of ARG_RESULT_DEST_VIEWNAME / ARG_RESULT_DEST_KEY)

ARG_RESULT_DEST_VIEWNAME: the destination to which the final result (e.g. ClazzEnrolment) will be
returned on completion

ARG_RESULT_DEST_KEY: The key (string) that will be used so that the screen which requested a value
can recognise what kind of value is incoming. In this case this would be the ClazzEnrolment

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

## Conventions

All Kotlin code should follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)

### Spelling

Use US English spellings, the same as system libraries etc.

### Localization strings

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



