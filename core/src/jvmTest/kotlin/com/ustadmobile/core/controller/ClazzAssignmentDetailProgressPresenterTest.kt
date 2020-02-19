package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzAssignmentDetailProgressView
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.util.test.AbstractSetup
import com.ustadmobile.util.test.checkJndiSetup
import org.junit.Before
import org.junit.After
import org.junit.Test


class ClazzAssignmentDetailProgressPresenterTest : AbstractSetup() {

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
            : Pair<ClazzAssignmentDetailProgressView, ClazzAssignmentDetailProgressPresenter> {
        val mockView = mock<ClazzAssignmentDetailProgressView> {
            on { runOnUiThread(any()) }.doAnswer {
                Thread(it.getArgument<Any>(0) as Runnable).start()
                Unit
            }
        }
        val mockContext = mock<DoorLifecycleOwner> {}
        val presenter = ClazzAssignmentDetailProgressPresenter(mockContext,
                presenterArgs, mockView, systemImplSpy)
        return Pair(mockView, presenter)
    }

    @Test
    fun givenPresenterCreated_whenEditClicked_shouldGoToEdit() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        presenter.handleClickEdit()
        verify(systemImplSpy, timeout(1000)).go(ClazzAssignmentEditView.VIEW_NAME,
                mapOf(UstadView.ARG_CLAZZ_ASSIGNMENT_UID to "42"))


    }

}