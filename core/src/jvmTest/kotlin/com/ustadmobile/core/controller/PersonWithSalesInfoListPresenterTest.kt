package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
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

        //TODO: make spy
        val systemImplSpy = spy(UstadMobileSystemImpl.instance)

        val presenter = PersonWithSaleInfoListPresenter(Any(),
                mapOf(PersonWithSaleInfoListView.ARG_LE_UID to weUid.toString()),
                mockView, systemImplSpy)

        presenter.onCreate(mapOf())

        verify(mockView).setWEListFactory(any())

    }

}