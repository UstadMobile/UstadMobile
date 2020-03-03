package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzAssignmentDetailAssignmentView
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.util.test.AbstractSetup
import com.ustadmobile.util.test.checkJndiSetup
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.Assert


class ClazzAssignmentDetailAssignmentPresenterTest : AbstractSetup() {

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
            : Pair<ClazzAssignmentDetailAssignmentView, ClazzAssignmentDetailAssignmentPresenter> {
        val mockView = mock<ClazzAssignmentDetailAssignmentView> {
            on { runOnUiThread(any()) }.doAnswer {
                Thread(it.getArgument<Any>(0) as Runnable).start()
                Unit
            }
        }
        val mockContext = mock<DoorLifecycleOwner> {}
        val presenter = ClazzAssignmentDetailAssignmentPresenter(mockContext,
                presenterArgs, mockView, systemImplSpy)
        return Pair(mockView, presenter)
    }

    @Test
    fun givenPresenterCreated_whenLoadedWithEditPermission_shouldUpdateView() {

        //Set active logged in account
        UmAccountManager.setActiveAccount(umAccount!!, Any(), UstadMobileSystemImpl.instance)

        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf(UstadView.ARG_CLAZZ_ASSIGNMENT_UID to "40",
                UstadView.ARG_CLAZZ_UID to "42"))

        verify(view, timeout(1000)).setEditVisibility(true)
        //TODO: Test setClazzAssignment
        verify(view, timeout(1000)).setClazzAssignment(any())
    }

    @Test
    fun givenPresenterCreated_whenLoadedWithoutEditPermission_shouldUpdateView() {

        //Set active logged in account
        UmAccountManager.setActiveAccount(randomAccount!!, Any(), UstadMobileSystemImpl.instance)

        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf(UstadView.ARG_CLAZZ_ASSIGNMENT_UID to "40",
                UstadView.ARG_CLAZZ_UID to "42"))

        verify(view, timeout(1000)).setEditVisibility(false)
        //TODO: Test setClazzAssignment
        verify(view, timeout(1000)).setClazzAssignment(any())
    }

    @Test
    fun givenPresenterCreated_whenEditClicked_shouldOpenEdit(){
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf(UstadView.ARG_CLAZZ_ASSIGNMENT_UID to "40",
                UstadView.ARG_CLAZZ_UID to "42"))

        presenter.handleClickEdit()

        verify(systemImplSpy, timeout(1000)).go(ClazzAssignmentDetailView.VIEW_NAME,
                mapOf(UstadView.ARG_CLAZZ_ASSIGNMENT_UID to "40",
                        UstadView.ARG_CLAZZ_UID to "42"))

    }

}