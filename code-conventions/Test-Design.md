# Test design conventions

The objective of the testing design is to:

* Ensure that code is tested in it's own module, and code coverage can be easily measured.
* Maximize speed of development and minimize time waiting for builds.

The order of development is:

* Presenter and view classes with the required method signatures
* Presenter unit test (which would iniitally fail because the code is
not yet implemented)
* Implementation of the required code in the presenter so that the 
presenter unit test passes. 
* Create activity with the required views, implementing the
view interface.
* Write the espresso test and allow it to fail, then implement required
 code on Android __or__ implement the 
required code in the activity, and then add record the test using
espresso test recorder.

This allows most of the code to be written and tested using JVM tests
that run and build much faster, without having to use an Android device
or emulator.

The tests for a given presenter are built as follows:

*  **AbstractScreenNamePresenterTest** - abstract class in the lib-test-common
module that contains shared test setup methods (e.g. database inserts etc.)

```
abstract class AbstractScreenNamePresenterTest {
    fun insertTestData(db: UmAppDatabase) {
        db.someDao.insertTestData(...)    
    }
}
```

* **ScreenNamePresenterTest** - Presenter Unit Test class in jvmTest 
source of the module containing ScreenNamePresenter. This class extends
AbstractScreenNamePresenterTest.

```
class ScreenNamePresenterTest: AbstractScreenNamePresenterTest() {

    lateinit var db: UmAppDatabase
    
    lateinit var mockView: ScreenNameView 
    
    @Before
    fun setup() {
        db = DatabaseBuilder.builder(Any(), UmAppDatabase::class, "db1")
        insertTestData(db)
        screenNameView = mock<ScreenNameView> {}
    }
    
    @Test
    fun givenUserInDb_whenSomeButtonClicked_thenViewAndDataShouldBeUpdated() {
        val testPresenter = ScreenNamePresenter(Any(), mapOf("argkey" to "value"),
            mockView)
            
        testPresenter.handleSomeButtonClicked()
        
        verify(mockView).showTitle("Expected title")
        Assert.assertEquals("Total updated", 
            10,
            db.someDao.findByUid(key).total)
    }
}
```

Unfortunately, the normal IDE test runner will not work as it doessn't 
get the classpath correct. Click the Gradle bar on the top right of the
IDE, select the module containing ScreenNamePresenterTest, select **tasks**,
and then select **verification**. You can then double click the **jvmTest**
 task to run the test. To run only one test, select the run configuration
 from the top, then select **Edit configurations**. Add the following to
 the arguments:
 ```
 --tests fully.qualified.name.of.ScreenNamePresenterTest
 ```

* **ScreenNameEspressoTest** - this is the Android UI test and can be used
to test the functionality across different Android versions. It is in
the androidTest source of the app-android module. Instead of writing
the espresso test manually, you can use **Record Espresso Test** in 
Android Studio (from the **Run** menu). To make the app start with the
activity you want to test, you can change the first destination in the
UstadMobileSystemImpl.startUI function. You can then copy/paste the
generated code into the espresso test itself.

```
class ScreenNameEspressoTest: AbstractScreenNamePresenterTest {
    @get:Rule
    var mActivityRule = IntentsTestRule(ScreenNameActivity::class.java, false, false)

    lateinit var context: Context

    @get:Rule
    var permissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION)
            
    @Before
    fun setup() {
        val db = UmAppDatabase.getInstance(InstrumentationRegistry.targetContext)
        insertTestData(db)
    }
    
    @Test
    fun givenUserInDb_whenSomeButtonClicked_thenViewAndDataShouldBeUpdated() {
        onView(withId(R.id.someView).click()
        
        ... espresso assertions here    
    }
    
}
```
