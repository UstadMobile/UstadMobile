package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.core.view.ClazzAssignmentListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.util.test.AbstractSetup
import com.ustadmobile.util.test.checkJndiSetup
import org.junit.After
import org.junit.Before
import org.junit.Test


class ClazzAssignmentListPresenterTest : AbstractSetup() {

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
            : Pair<ClazzAssignmentListView, ClazzAssignmentListPresenter> {
        val mockView = mock<ClazzAssignmentListView> {
            on { runOnUiThread(any()) }.doAnswer {
                Thread(it.getArgument<Any>(0) as Runnable).start()
                Unit
            }
        }
        val mockContext = mock<DoorLifecycleOwner> {}
        val presenter = ClazzAssignmentListPresenter(mockContext,
                presenterArgs, mockView, systemImplSpy)
        return Pair(mockView, presenter)
    }

    @Test
    fun givenPresenterCreated_whenClickAssignment_shouldCallGoToDetail() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf(UstadView.ARG_CLAZZ_UID to "21"))

        val ca = ClazzAssignmentWithMetrics()
        ca.clazzAssignmentUid = 42

        presenter.handleClickAssignment(ca)

        verify(systemImplSpy, timeout(1000)).go(ClazzAssignmentDetailView.VIEW_NAME,
                mapOf(UstadView.ARG_CLAZZ_ASSIGNMENT_UID to "42",
                        UstadView.ARG_CLAZZ_UID to "21"))

    }

    @Test
    fun givenPresenterCreated_whenClickNewAssignment_shouldCallGoToDetail() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf(UstadView.ARG_CLAZZ_UID to "21"))


        presenter.handleClickNewAssignment()

        verify(systemImplSpy, timeout(1000)).go(ClazzAssignmentDetailView.VIEW_NAME,
                mapOf(UstadView.ARG_CLAZZ_ASSIGNMENT_UID to "0",
                        UstadView.ARG_CLAZZ_UID to "21"))

    }

}