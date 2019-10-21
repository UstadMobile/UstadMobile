package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.BasePointView2
import com.ustadmobile.core.view.SettingsView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.util.test.AbstractSaleRelatedSetup
import com.ustadmobile.util.test.checkJndiSetup
import org.junit.Before
import org.junit.Test

class BasePoint2PresenterTest:AbstractSaleRelatedSetup() {

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
            : Pair<BasePointView2, BasePointActivity2Presenter> {
        val mockView = mock<BasePointView2> {}
        val mockContext = mock<DoorLifecycleOwner> {}
        val presenter = BasePointActivity2Presenter(mockContext,
                presenterArgs, mockView, systemImplSpy)
        return Pair(mockView, presenter)
    }

    @Test
    fun givenPresenterCreated_whenHandleClickSettingsCalled_thenShouldCallGo(){
        // create presenter, with a mock view, check that it makes that call
        val (_, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        presenter.handleClickSettingsIcon()
        verify(systemImplSpy).go(eq(SettingsView.VIEW_NAME), eq(mapOf()), any())

    }
}