package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.util.test.AbstractSetup
import com.ustadmobile.util.test.checkJndiSetup
import org.junit.Before
import org.junit.After
import org.junit.Assert
import org.junit.Test


class ClazzDetailEnrollStudentPresenterTest : AbstractSetup() {

    lateinit var systemImplSpy: UstadMobileSystemImpl


    @Before
    fun setUp() {
        checkJndiSetup()
        val impl = UstadMobileSystemImpl.instance

        val db = UmAppDatabase.getInstance(Any())

        //do inserts
        insert(db, true)

        //Set active logged in account
        UmAccountManager.setActiveAccount(umAccount!!, Any(), impl)
        systemImplSpy = spy(impl)

    }

    @After
    fun tearDown() {
    }


    fun createMockViewAndPresenter(presenterArgs: Map<String, String> = mapOf())
            : Pair<ClazzDetailEnrollStudentView, ClazzDetailEnrollStudentPresenter> {
        val mockView = mock<ClazzDetailEnrollStudentView> {
            on { runOnUiThread(any()) }.doAnswer {
                Thread(it.getArgument<Any>(0) as Runnable).start()
                Unit
            }
        }
        val mockContext = mock<DoorLifecycleOwner> {}
        val presenter = ClazzDetailEnrollStudentPresenter(mockContext,
                presenterArgs, mockView, systemImplSpy)
        return Pair(mockView, presenter)
    }

    @Test
    fun givenPresenterCreated_whenAllValidArguments_shouldSetProviderOnView() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        //TODO: First test here
        Assert.assertTrue(true)

    }

    @Test
    fun givenPresenterCreatedWithArgs_whenHandleClickEnrollNewPersonClicked_shouldCallGoWithValidArgs() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        //TODO: First test here
        Assert.assertTrue(true)

    }

    @Test
    fun givenPresenterCreated_whenSecondaryPressed_shouldInsertOrUpdateGroupMmberAndSetToActiveOrInactive() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        //TODO: First test here
        Assert.assertTrue(true)

        //For both with args and without

    }

    @Test
    fun givenPresenterCreated_whenHandleClickDone_shouldCallFinishOnView() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        //TODO: First test here
        Assert.assertTrue(true)

    }

}