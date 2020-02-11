//package com.ustadmobile.core.controller
//
//import com.nhaarman.mockitokotlin2.*
//import com.ustadmobile.core.db.UmAppDatabase
//import com.ustadmobile.core.impl.UstadMobileSystemImpl
//import com.ustadmobile.core.view.PersonWithSaleInfoDetailView.Companion.ARG_WE_UID
//import com.ustadmobile.core.view.SaleDetailView
//import com.ustadmobile.core.view.SaleListSearchView
//import com.ustadmobile.core.view.SaleListView
//import com.ustadmobile.door.DoorLifecycleObserver
//import com.ustadmobile.door.DoorLifecycleOwner
//import com.ustadmobile.util.test.AbstractSaleRelatedSetup
//import com.ustadmobile.util.test.checkJndiSetup
//import org.junit.Before
//import org.junit.Test
//import org.mockito.Mockito
//
//
//class SaleListPresenterTest : AbstractSaleRelatedSetup() {
//
//    lateinit var systemImplSpy: UstadMobileSystemImpl
//    private lateinit var context: DoorLifecycleOwner
//
//    @Before
//    fun setup(){
//        checkJndiSetup()
//        systemImplSpy = spy(UstadMobileSystemImpl.instance)
//        val db = UmAppDatabase.getInstance(Any())
//
//        //Inserts
//        insert(db)
//    }
//
//    fun createMockViewAndPresenter(presenterArgs: Map<String, String>? =
//                                           mapOf(ARG_WE_UID to we1PersonUid.toString())
//    ): Pair<SaleListView,SaleListPresenter> {
//        val mockView = mock<SaleListView> {
//
//        }
//
//        context  = mock{
//            on{
//                currentState
//            }.thenReturn(DoorLifecycleObserver.STARTED)
//        }
//
//        val presenter = SaleListPresenter(
//                context,
//                presenterArgs,
//                mockView,
//                UmAppDatabase.getInstance(context),
//                systemImplSpy)
//        return Pair(mockView, presenter)
//    }
//
//    @Test
//    fun givenPresenter_whenOnCreateCalled_thenShouldUpdateView(){
//        //Create presenter, with a mock view, check that it makes that call
//        val (mockView, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        verify(mockView, Mockito.timeout(1000)).setListProvider(any(),
//                eq(false), eq(false))
//        verify(mockView, Mockito.timeout(1000)).updateSortSpinner(any())
//
//        //TODO: Fix when Observe is fixed
//        //verify(mockView, Mockito.timeout(2000)).updatePaymentDueCounter(2)
//        //verify(mockView, Mockito.timeout(2000)).updatePreOrderCounter(2)
//    }
//
//
//    @Test
//    fun givenPresenter_whenFilterAllCalled_thenShouldUpdateView(){
//        //Create presenter, with a mock view, check that it makes that call
//        val (mockView, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        presenter.filterAll()
//        verify(mockView, Mockito.timeout(1000).times(2)).setListProvider(any(),
//                eq(false), eq(false))
//    }
//
//    @Test
//    fun givenPresenter_whenFilterPreOrderCalled_thenShouldUpdateView(){
//        //Create presenter, with a mock view, check that it makes that call
//        val (mockView, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        presenter.filterAll()
//        verify(mockView, Mockito.timeout(1000).times(2)).setListProvider(any(),
//                eq(false), eq(false))
//    }
//
//    @Test
//    fun givenPresenter_whenFilterPaymentDueCalled_thenShouldUpdateView(){
//        //Create presenter, with a mock view, check that it makes that call
//        val (mockView, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        presenter.filterAll()
//        verify(mockView, Mockito.timeout(1000).times(2)).setListProvider(any(),
//                eq(false), eq(false))
//    }
//
//    @Test
//    fun givenPresenter_whenClickSale_thenShouldCallImpl(){
//        //Create presenter, with a mock view, check that it makes that call
//        val (_, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        presenter.handleClickSale(sale11Uid, null)
//        verify(systemImplSpy, Mockito.timeout(1000)).go(eq(SaleDetailView.VIEW_NAME),
//                eq(mapOf(SaleDetailView.ARG_SALE_UID to sale11Uid.toString())), any())
//    }
//
//    @Test
//    fun givenPresenter_whenClickNewSale_thenShouldCallImpl(){
//        //Create presenter, with a mock view, check that it makes that call
//        val (_, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        presenter.handleClickPrimaryActionButton()
//        verify(systemImplSpy).go(eq(SaleDetailView.VIEW_NAME), eq(mapOf()), any())
//    }
//
//    @Test
//    fun givenPresenter_whenClickSearch_thenShouldCallImpl(){
//        //Create presenter, with a mock view, check that it makes that call
//        val (_, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        presenter.handleClickSearch()
//        verify(systemImplSpy).go(eq(SaleListSearchView.VIEW_NAME), eq(mapOf()), any())
//    }
//
//    @Test
//    fun givenPresenter_whenHandleCommonPressed_shouldCallClickSale(){
//        //Create presenter, with a mock view, check that it makes that call
//        val (_, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        presenter.handleCommonPressed(sale11Uid, "")
//        verify(systemImplSpy).go(eq(SaleDetailView.VIEW_NAME),
//                eq(mapOf(SaleDetailView.ARG_SALE_UID to sale11Uid.toString())), any())
//    }
//
//
//    @Test
//    fun givenPresenter_whenSecondaryPressed_doesNothing(){
//        //Create presenter, with a mock view, check that it makes that call
//        val (_, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        presenter.handleSecondaryPressed("")
//
//    }
//
//    @Test
//    fun givenPresenter_whenHandlePreOrderCountUpdatePressed_updatesCountOnView(){
//        //Create presenter, with a mock view, check that it makes that call
//        val (mockView, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        presenter.handlePreOrderCountUpdate(4)
//        verify(mockView, Mockito.timeout(2000)).updatePreOrderCounter(4)
//    }
//
//
//    @Test
//    fun givenPresenter_whenHandlePaymentDueCountUpdatePressed_updatesCountOnView(){
//        //Create presenter, with a mock view, check that it makes that call
//        val (mockView, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        presenter.handlePaymentDueCountUpdate(4)
//        verify(mockView, Mockito.timeout(2000)).updatePaymentDueCounter(4)
//
//    }
//
//    @Test
//    fun givenPresenter_whenChangeSortOrder_shouldUpdateProvider(){
//        //Create presenter, with a mock view, check that it makes that call
//        val (mockView, presenter) = createMockViewAndPresenter()
//        presenter.onCreate(mapOf())
//
//        presenter.handleChangeSortOrder(1)
//        verify(mockView, Mockito.timeout(1000).times(2)).setListProvider(any(),
//                eq(false), eq(false))
//
//
//    }
//
//}
