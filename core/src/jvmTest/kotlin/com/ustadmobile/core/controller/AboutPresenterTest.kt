//package com.ustadmobile.core.controller
//
//import org.mockito.kotlin.*
//import com.ustadmobile.core.db.UmAppDatabase
//import com.ustadmobile.core.impl.UmAccountManager
//import com.ustadmobile.core.impl.UstadMobileSystemImpl
//import com.ustadmobile.core.view.AboutView
//import com.ustadmobile.door.lifecycle.LifecycleOwner
//import com.ustadmobile.util.test.AbstractSetup
//import com.ustadmobile.util.test.checkJndiSetup
//import org.junit.Before
//import org.junit.After
//import org.junit.Assert
//import org.junit.Test
//
//
//
//class AboutPresenterTest : AbstractSetup() {
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
//    }
//
//    @After
//    fun tearDown() {
//
//    }
//
//    fun createMockViewAndPresenter(presenterArgs: Map<String, String> = mapOf())
//            : Pair<AboutView, AboutPresenter> {
//        val mockView = mock<AboutView> {
//            on { runOnUiThread(any()) }.doAnswer {
//                Thread(it.getArgument<Any>(0) as Runnable).start()
//                Unit
//            }
//        }
//        val mockContext = mock<LifecycleOwner> {}
//        val presenter = AboutPresenter(mockContext,
//                presenterArgs, mockView, systemImplSpy)
//        return Pair(mockView, presenter)
//    }
//
//    @Test
//    fun givenPresenterCreated_whenLoaded_shouldSetVersionInfo() {
//        // create presenter, with a mock view, check that it makes that call
//        val (view, presenter) = createMockViewAndPresenter()
//        //presenter.onCreate(mapOf())
//
//        //TODO: First test here
//
//        Assert.assertTrue(true)
//    }
//
//    @Test
//    fun givenPresenterCreated_whenLoaded_shouldSetAboutHTML() {
//        // create presenter, with a mock view, check that it makes that call
//        val (view, presenter) = createMockViewAndPresenter()
//        //presenter.onCreate(mapOf())
//
//        Assert.assertTrue(true)
//
//        //TODO: First test here
//
//    }
//
//}