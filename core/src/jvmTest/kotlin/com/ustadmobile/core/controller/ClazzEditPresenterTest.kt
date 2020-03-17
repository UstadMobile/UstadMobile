//package com.ustadmobile.core.controller
//
//import com.nhaarman.mockitokotlin2.*
//import com.ustadmobile.core.db.UmAppDatabase
//import com.ustadmobile.core.impl.UmAccountManager
//import com.ustadmobile.core.impl.UstadMobileSystemImpl
//import com.ustadmobile.core.view.ClazzEditView
//import com.ustadmobile.door.DoorLifecycleOwner
//import com.ustadmobile.util.test.AbstractSetup
//import com.ustadmobile.util.test.checkJndiSetup
//import org.junit.Before
//import org.junit.After
//import org.junit.Assert
//import org.junit.Test
//
//
//class ClazzEditPresenterTest : AbstractSetup() {
//
//    lateinit var systemImplSpy: UstadMobileSystemImpl
//
//
//    @Before
//    fun setUp() {
//        checkJndiSetup()
//        val impl = UstadMobileSystemImpl.instance
//
//        val db = UmAppDatabase.getInstance(Any())
//
//        //do inserts
//        insert(db, true)
//
//        //Set active logged in account
//        UmAccountManager.setActiveAccount(umAccount!!, Any(), impl)
//        systemImplSpy = spy(impl)
//
//    }
//
//    @After
//    fun tearDown() {
//    }
//
//
//    fun createMockViewAndPresenter(presenterArgs: Map<String, String> = mapOf())
//            : Pair<ClazzEditView, ClazzEditPresenter> {
//        val mockView = mock<ClazzEditView> {
//            on { runOnUiThread(any()) }.doAnswer {
//                Thread(it.getArgument<Any>(0) as Runnable).start()
//                Unit
//            }
//        }
//        val mockContext = mock<DoorLifecycleOwner> {}
//        val presenter = ClazzEditPresenter(mockContext,
//                presenterArgs, mockView, systemImplSpy)
//        return Pair(mockView, presenter)
//    }
//
//    @Test
//    fun givenPresenterCreated_whenLoaded_shouldClearAllCustomFieldsOnView() {
//        // create presenter, with a mock view, check that it makes that call
//        val (view, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        //TODO: First test here
//        Assert.assertTrue(true)
//
//    }
//
//    @Test
//    fun givenPresenterCreated_whenLoaded_shouldUpdateAllCustomFieldsOnViewForTextAndDropdown() {
//        // create presenter, with a mock view, check that it makes that call
//        val (view, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        //TODO: First test here
//        Assert.assertTrue(true)
//
//    }
//
//    @Test
//    fun givenPresenterCreatedWithoutClazzUid_whenLoaded_shouldCreateNewClazzOnView() {
//        // create presenter, with a mock view, check that it makes that call
//        val (view, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        //TODO: First test here
//        Assert.assertTrue(true)
//
//    }
//
//    @Test
//    fun givenPresenterCreatedWithClazzUid_whenLoaded_shouldUpdateClazzOnView() {
//        // create presenter, with a mock view, check that it makes that call
//        val (view, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        //TODO: First test here
//        Assert.assertTrue(true)
//
//    }
//
//    @Test
//    fun givenPresenterCreated_whenLoaded_shouldSetHolidaysPresetsOnViewIfAny() {
//        // create presenter, with a mock view, check that it makes that call
//        val (view, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        //TODO: First test here
//        Assert.assertTrue(true)
//
//    }
//
//    @Test
//    fun givenPresenterCreated_whenLoaded_shouldSetLocationPresetsOnView() {
//        // create presenter, with a mock view, check that it makes that call
//        val (view, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        //TODO: First test here
//        Assert.assertTrue(true)
//
//    }
//
//    @Test
//    fun givenPresenterCreated_whenLoaded_shouldUpdateScheduleProvideOnView() {
//        // create presenter, with a mock view, check that it makes that call
//        val (view, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        //TODO: First test here
//        Assert.assertTrue(true)
//
//    }
//
//    @Test
//    fun givenPresenterCreated_whenUpdateFeaturesCalledAndSaved_shouldUpdateFeaturesToEditingClazz() {
//        // create presenter, with a mock view, check that it makes that call
//        val (view, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        //TODO: First test here
//        Assert.assertTrue(true)
//    }
//
//    @Test
//    fun givenPresenterCreated_whenUpdateNameAndDescCalled_shouldPersistWhenDone() {
//        // create presenter, with a mock view, check that it makes that call
//        val (view, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        //TODO: First test here
//        Assert.assertTrue(true)
//
//    }
//
//    @Test
//    fun givenPresenterCreated_whenUpdateHolidaySelected_shouldPersistWhenSaved() {
//        // create presenter, with a mock view, check that it makes that call
//        val (view, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        //TODO: First test here
//        Assert.assertTrue(true)
//
//    }
//
//    @Test
//    fun givenPresenterCreated_whenUpdateLocationSelected_shouldPersistWhenSaved() {
//        // create presenter, with a mock view, check that it makes that call
//        val (view, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        //TODO: First test here
//        Assert.assertTrue(true)
//
//    }
//
//    @Test
//    fun givenPresenterCreated_whenHandleClickAddScheduleClicked_shouldCallImplGoToAddScheduleDialog() {
//        // create presenter, with a mock view, check that it makes that call
//        val (view, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        //TODO: First test here
//        Assert.assertTrue(true)
//
//    }
//
//    @Test
//    fun givenPresenterCreated_whenHandleSaveCustomFieldsValueCalled_shouldPersistNewValues() {
//        // create presenter, with a mock view, check that it makes that call
//        val (view, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        //TODO: First test here
//        Assert.assertTrue(true)
//
//    }
//
//    @Test
//    fun givenPresenterCreated_whenHandleClickDoneCalled_shouldPersistClazzAndFinishView(){
//        // create presenter, with a mock view, check that it makes that call
//        val (view, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        //TODO: First test here
//        Assert.assertTrue(true)
//    }
//
//    @Test
//    fun givenPresenterCreatedWithNoClazzUid_whenHandleClickDoneCalled_shouldPersistNewClazzLocation(){
//        // create presenter, with a mock view, check that it makes that call
//        val (view, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        //TODO: First test here
//        Assert.assertTrue(true)
//    }
//}