package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.PersonWithSaleInfoDetailView
import com.ustadmobile.core.view.PersonWithSaleInfoDetailView.Companion.ARG_WE_UID
import com.ustadmobile.util.test.checkJndiSetup
import org.junit.Before
import org.junit.Test

class PersonWithSaleInfoDetailPresenterTest: AbstractSaleRelatedSetup(){
    var weUid : Long = 0L

    @Before
    fun setup(){
        checkJndiSetup()
        val db = UmAppDatabase.getInstance(Any())

        //Inserts
        insert(db)
    }

    @Test
    fun givenPresenter_whenOnCreateCalled_thenShouldSetFactoryAndUpdateView(){
        //Create presenter, with a mock view, check that it makes that call
        val mockView = mock<PersonWithSaleInfoDetailView>{}
        val systemImplSpy = spy(UstadMobileSystemImpl.instance)

        val presenter = PersonWithSaleInfoDetailPresenter(Any(),
                mapOf(ARG_WE_UID to weUid.toString()), mockView, systemImplSpy)
        presenter.onCreate(mapOf())

        verify(mockView).setSalesFactory(any())

        verify(mockView).updatePersonOnView(any())
    }

}