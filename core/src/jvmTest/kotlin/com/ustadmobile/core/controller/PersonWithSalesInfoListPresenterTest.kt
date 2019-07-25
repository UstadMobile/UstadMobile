package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.PersonWithSaleInfoDetailView
import com.ustadmobile.core.view.PersonWithSaleInfoDetailView.Companion.ARG_WE_UID
import com.ustadmobile.core.view.PersonWithSaleInfoListView
import com.ustadmobile.util.test.checkJndiSetup
import org.junit.Before
import org.junit.Test

class PersonWithSalesInfoListPresenterTest : AbstractPersonWithSalesInfoListPresenterTest(){

    var weUid: Long = 0L

    @Before
    fun setUp() {
        checkJndiSetup()
        val db = UmAppDatabase.getInstance(Any())

        //do inserts
        insert(db)


    }

    @Test
    fun givenPresenter_whenOnCreateCalled_thenShouldSetFactor(){
        // create preenter, with a mock view, check that it makes that call
        val mockView = mock<PersonWithSaleInfoListView> {

        }

        val systemImplSpy = spy(UstadMobileSystemImpl.instance)

        val presenter = PersonWithSaleInfoListPresenter(Any(),
                mapOf(PersonWithSaleInfoListView.ARG_LE_UID to weUid.toString()),
                mockView, systemImplSpy)

        presenter.onCreate(mapOf())

        verify(mockView).setWEListFactory(any())

    }

    @Test
    fun givenPresenter_whenHandleClickWECalled_thenShouldCallGo(){
        val mockView = mock<PersonWithSaleInfoListView> {
        }

        val systemImplSpy = spy(UstadMobileSystemImpl.instance)

        val presenter = PersonWithSaleInfoListPresenter(Any(),
                mapOf(PersonWithSaleInfoListView.ARG_LE_UID to weUid.toString()),
                mockView, systemImplSpy)
        presenter.onCreate(mapOf())

        presenter.handleClickWE(weUid)
        verify(systemImplSpy).go(eq(PersonWithSaleInfoDetailView.VIEW_NAME),
                eq(mapOf(ARG_WE_UID to weUid.toString())), any())
    }

}