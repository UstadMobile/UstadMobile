//package com.ustadmobile.core.controller
//
//import com.nhaarman.mockitokotlin2.*
//import com.ustadmobile.core.db.UmAppDatabase
//import com.ustadmobile.core.impl.UmAccountManager
//import com.ustadmobile.core.impl.UstadMobileSystemImpl
//import com.ustadmobile.core.view.AddScheduleDialogView
//import com.ustadmobile.door.DoorLifecycleOwner
//import com.ustadmobile.util.test.AbstractSetup
//import com.ustadmobile.util.test.checkJndiSetup
//import org.junit.Before
//import org.junit.After
//import org.junit.Assert
//import org.junit.Test
//
//
//class AddScheduleDialogPresenterTest : AbstractSetup() {
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
//            : Pair<AddScheduleDialogView, AddScheduleDialogPresenter> {
//        val mockView = mock<AddScheduleDialogView> {
//            on { runOnUiThread(any()) }.doAnswer {
//                Thread(it.getArgument<Any>(0) as Runnable).start()
//                Unit
//            }
//        }
//        val mockContext = mock<DoorLifecycleOwner> {}
//        val presenter = AddScheduleDialogPresenter(mockContext,
//                presenterArgs, mockView, systemImplSpy)
//        return Pair(mockView, presenter)
//    }
//
//    @Test
//    fun givenPresenterCreated_whenClazzUidAndScheduleUidGiven_shouldUpdateFields() {
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
//    fun givenPresenterCreated_whenScheduleUidNotGiven_shouldCreateNewSchedule() {
//        // create presenter, with a mock view, check that it makes that call
//        val (view, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        //TODO: First test here
//        Assert.assertTrue(true)
//    }
//
//    @Test
//    fun givenPresenterCreated_whenClazzUidNotGiven_shouldSetClazzToNothing() {
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
//    fun givenPresenterCreated_whenHandleCancelScheduleClicked_shouldNullAndNotPersistAndDoNothing() {
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
//    fun givenPresenterCreated_whenHandleScheduleFromTimeSelcted_shouldSetStartTime() {
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
//    fun givenPresenterCreated_whenHandleScheduleToTimeSelected_shouldSetToTime() {
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
//    fun givenPresenterCreated_whenHandleScheduleSelected_shouldSetScheduleDayAndCallHideDayPickerOnView() {
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
//    fun givenPresenterCreated_whenHandleDaySelected_shouldSetDay() {
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
//    fun givenPresenterCreatedAsNew_whenHandleAddScheduleCalled_shouldInsertSchedule() {
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
//    fun givenPresenterCreatedAsExisting_whenHandleAddSCheduleCalled_shouldUpdateScheduleAndCancelFutureInstances() {
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
//    fun givenPresenterCreated_whenHandleAddScheduleCalled_shouldCreateClazzLogsForTodayAndScheduleChecks() {
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
//    fun givenPresenterNotCreated_whenCreatePendingScheduleChecksCalled_shouldCreateScheduledChecks(){
//
//        //Directly call and test ?
//
//        //TODO: First test here
//        Assert.assertTrue(true)
//
//    }
//
//}