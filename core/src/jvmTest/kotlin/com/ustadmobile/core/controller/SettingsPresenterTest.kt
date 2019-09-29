package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.util.test.AbstractSaleRelatedSetup
import com.ustadmobile.util.test.checkJndiSetup
import org.junit.Before
import org.junit.Test

class SettingsPresenterTest : AbstractSaleRelatedSetup() {

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
            : Pair<SettingsView, SettingsPresenter> {
        val mockView = mock<SettingsView> {}
        val mockContext = mock<DoorLifecycleOwner> {}
        val presenter = SettingsPresenter(mockContext,
                presenterArgs, mockView, systemImplSpy)
        return Pair(mockView, presenter)
    }

    @Test
    fun givenPresenterCreated_whenGoToGroupList_shouldCallImplGoWithGroup() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        presenter.goToGroupsList()
        verify(systemImplSpy).go(eq(GroupListView.VIEW_NAME), eq(mapOf()), any())
    }

    @Test
    fun givenPresenterCreated_whenGoToLocationList_shouldCallImplGoWithLocation() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        presenter.goToLocationsList()
        verify(systemImplSpy).go(eq(LocationListView.VIEW_NAME), eq(mapOf()), any())
    }

    @Test
    fun givenPresenterCreated_whenGoToPeopleList_shouldCallImplGoWithPeople() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        presenter.goToPeopleList()
        verify(systemImplSpy).go(eq(PeopleListView.VIEW_NAME), eq(mapOf()), any())
    }

}