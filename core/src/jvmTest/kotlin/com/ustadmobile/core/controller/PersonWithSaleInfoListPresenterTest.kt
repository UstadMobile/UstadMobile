package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SaleDao.Companion.SORT_ORDER_NAME_ASC
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.PersonWithSaleInfoDetailView
import com.ustadmobile.core.view.PersonWithSaleInfoDetailView.Companion.ARG_WE_UID
import com.ustadmobile.core.view.PersonWithSaleInfoListView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.util.test.AbstractSaleRelatedSetup
import com.ustadmobile.util.test.checkJndiSetup
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class PersonWithSaleInfoListPresenterTest : AbstractSaleRelatedSetup(){

    lateinit var systemImplSpy: UstadMobileSystemImpl

    @Before
    fun setUp() {
        checkJndiSetup()
        systemImplSpy = spy(UstadMobileSystemImpl.instance)
        val db = UmAppDatabase.getInstance(Any())

        //do inserts
        insert(db, true)

    }

    data class SomeSetupInfo(val first: Any, val second: Any, val third: Any)

    fun createMockViewAndPresenter(presenterArgs: Map<String, String> =
               mapOf(PersonWithSaleInfoListView.ARG_LE_UID to le1Uid.toString())
    ): Pair<PersonWithSaleInfoListView,PersonWithSaleInfoListPresenter> {
        val mockView = mock<PersonWithSaleInfoListView> {

        }
        val mockContext = mock<DoorLifecycleOwner> {}


        val presenter = PersonWithSaleInfoListPresenter(Any(),
                presenterArgs, mockView, systemImplSpy, UmAppDatabase.getInstance(mockContext))
        return Pair(mockView, presenter)
    }

    @Test
    fun givenPresenter_whenOnCreateCalled_thenShouldSetFactor(){
        // create presenter, with a mock view, check that it makes that call
        val (mockView, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        //TODO: Figure this out
//        verify(mockView, Mockito.timeout(1000)).setWEListFactory(any())

    }

    @Test
    fun givenPresenter_whenOnCreateCalled_thenShouldUpdateSortPresets(){
        // create presenter, with a mock view, check that it makes that call
        val (mockView, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        verify(mockView, Mockito.timeout(1000)).updateSortSpinner(any())
    }

    @Test
    fun givenPresenter_whenHandleClickWECalled_thenShouldCallGo(){
        val (_, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        presenter.handleClickWE(le1Uid)
        verify(systemImplSpy).go(eq(PersonWithSaleInfoDetailView.VIEW_NAME),
                eq(mapOf(ARG_WE_UID to le1Uid.toString())), any())
    }

    @Test
    fun givenPresenter_whenSearchClicked_thenShouldCallGo(){
        val (mockView, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        presenter.handleSearchQuery("potato")
        verify(mockView, Mockito.timeout(1000).times(1)).setWEListFactory(any())

    }

    @Test
    fun givenPresenter_whenHandleSortChanged_thenShouldCallFactory(){
        // create presenter, with a mock view, check that it makes that call
        val (mockView, presenter) = createMockViewAndPresenter()

        presenter.onCreate(mapOf())

        presenter.handleSortChanged(SORT_ORDER_NAME_ASC.toLong())

        verify(mockView, Mockito.timeout(1000).times(1))
                .setWEListFactory(any())

    }


}