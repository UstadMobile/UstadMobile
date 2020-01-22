package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.PersonWithSaleInfoDetailView.Companion.ARG_WE_UID
import com.ustadmobile.core.view.PersonWithSaleInfoProfileView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.util.test.AbstractSaleRelatedSetup
import com.ustadmobile.util.test.checkJndiSetup
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito


class PersonWithSaleInfoProfilePresenterTest : AbstractSaleRelatedSetup() {

    lateinit var systemImplSpy: UstadMobileSystemImpl

    @Before
    fun setup(){
        checkJndiSetup()
        systemImplSpy = spy(UstadMobileSystemImpl.instance)
        val db = UmAppDatabase.getInstance(Any())

        //Inserts
        insert(db)
    }

    fun createMockViewAndPresenter(presenterArgs: Map<String, String> =
                                           mapOf(ARG_WE_UID to we1PersonUid.toString())
    ): Pair<PersonWithSaleInfoProfileView,PersonWithSaleInfoProfilePresenter> {
        val mockView = mock<PersonWithSaleInfoProfileView> {

        }

        val mockContext = mock<DoorLifecycleOwner> {}
        val presenter = PersonWithSaleInfoProfilePresenter(Any(),
                presenterArgs, mockView, UmAppDatabase.getInstance(mockContext))
        return Pair(mockView, presenter)
    }

    @Test
    fun givenPresenter_whenOnCreateCalled_thenShouldUpdateView(){
        //Create presenter, with a mock view, check that it makes that call
        val (mockView, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        //TODO: Figure this out
        //verify(mockView, Mockito.timeout(1000)).updatePersonOnView(any())

        //TODO: KMP Verify Peron Picture as well
        //verify(mockView, Mockito.timeout(1000)).updateImageOnView(any())
    }
}
