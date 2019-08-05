package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.util.test.AbstractSaleRelatedSetup
import com.ustadmobile.util.test.checkJndiSetup
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class PersonListPresenterTest : AbstractSaleRelatedSetup() {

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

    fun createMockViewAndPresenter(presenterArgs: Map<String, String> = mapOf())
            : Pair<PersonListView, PersonListPresenter> {
        val mockView = mock<PersonListView> {}
        val mockContext = mock<DoorLifecycleOwner> {}
        val presenter = PersonListPresenter(mockContext,
                presenterArgs, mockView, systemImplSpy)
        return Pair(mockView, presenter)
    }

    @Test
    fun givenPresenterCreated_whenCreated_shouldUpdateFactoryOnView() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        verify(view, Mockito.timeout(1000)).setProvider(any())

    }

    @Test
    fun givenPresenterCreated_whenHandleClickUser_shouldCallGoWithArgs(){
        val (_, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        presenter.handleClickUser(le1Uid)
        verify(systemImplSpy).go(eq(PersonDetailView.VIEW_NAME),
                eq(mapOf( PersonDetailView.ARG_PERSON_UID to le1Uid.toString())), any())
    }

    @Test
    fun givenPresenterCreated_whenHandleClickNewUser_shouldCallGoWithArgs(){
        val (_, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        presenter.handleClickAddUser()
        verify(systemImplSpy).go(eq(PersonDetailView.VIEW_NAME), eq(mapOf( )), any())
    }

}