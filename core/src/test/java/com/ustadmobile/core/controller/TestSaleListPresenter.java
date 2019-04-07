package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.SaleDao;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SaleDetailView;
import com.ustadmobile.core.view.SaleListView;
import com.ustadmobile.lib.db.entities.Sale;
import com.ustadmobile.lib.db.entities.SaleListDetail;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Hashtable;

import static com.ustadmobile.core.util.SetUpUtil.addDummyData;
import static com.ustadmobile.core.view.SaleDetailView.ARG_SALE_UID;
import static com.ustadmobile.test.core.util.CoreTestUtil.TEST_URI;
import static com.ustadmobile.test.core.util.CoreTestUtil.startServer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class TestSaleListPresenter {

    UstadMobileSystemImpl mainImpl;

    UstadMobileSystemImpl systemImplSpy;

    private HttpServer server;

    private UmAppDatabase db;

    private UmAppDatabase repo;

    private SaleListView mockView;


    @Before
    public void setUp(){


        mainImpl = UstadMobileSystemImpl.getInstance();
        systemImplSpy = Mockito.spy(mainImpl);
        UstadMobileSystemImpl.setMainInstance(systemImplSpy);
        server = startServer();

        db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        repo = db.getRepository(TEST_URI, "");

        db.clearAllTables();

        addDummyData(repo);

        mockView = Mockito.mock(SaleListView.class);
        doAnswer((invocationOnMock) -> {
            new Thread(((Runnable)invocationOnMock.getArgument(0))).start();
            return null;
        }).when(mockView).runOnUiThread(any());
    }

    @After
    public void tearDown() {
        UstadMobileSystemImpl.setMainInstance(mainImpl);
        systemImplSpy = null;
        server.shutdownNow();
    }

    @Test
    public void givenWhenSaleEntriesCreated_whenViewAndPresenterLoads_shouldReturnData(){

        Hashtable args = new Hashtable();
        SaleListPresenter presenter = new SaleListPresenter(PlatformTestUtil.getTargetContext(),
                args, mockView);
        presenter.onCreate(args);

        Assert.assertTrue(true);
        SaleDao saleDao = repo.getSaleDao();
        UmProvider<SaleListDetail> privoder = saleDao.findAllActiveAsSaleListDetailProvider();
        verify(mockView, timeout(5000)).setListProvider(privoder);

    }

    @Test
    public void givenWhenSaleEntriesCreated_whenPreOrdersClicked_shouldReturnData(){

        Hashtable args = new Hashtable();
        SaleListPresenter presenter = new SaleListPresenter(PlatformTestUtil.getTargetContext(),
                args, mockView);
        presenter.onCreate(args);

        Assert.assertTrue(true);
        SaleDao saleDao = repo.getSaleDao();

        verify(mockView, timeout(5000)).setListProvider(saleDao.findAllActiveSaleListDetailPreOrdersProvider());

    }

    @Test
    public void givenWhenSaleEntriesCreated_whenPaymnetClicked_shouldReturnData(){

        Hashtable args = new Hashtable();
        SaleListPresenter presenter = new SaleListPresenter(PlatformTestUtil.getTargetContext(),
                args, mockView);
        presenter.onCreate(args);

        Assert.assertTrue(true);
        SaleDao saleDao = repo.getSaleDao();

        verify(mockView, timeout(5000)).setListProvider(
                saleDao.findAllActiveSaleListDetailPaymentDueProvider());

    }

    @Test
    public void givenWhenPresenterCreated_whenHandleClickSaleCalled_shouldGoToSaleDetail() throws InterruptedException {

        Hashtable<String, String> args = new Hashtable<>();
        SaleListPresenter presenter = new SaleListPresenter(PlatformTestUtil.getTargetContext(),
                args, mockView);

        Thread.sleep(2000);
        //Get sale
        Sale testSale = repo.getSaleDao().findAllActiveList().get(0);
        presenter.handleClickSale(testSale.getSaleUid());

        Hashtable<String, String> goArgs = new Hashtable<>();
        goArgs.put(ARG_SALE_UID, String.valueOf(testSale.getSaleUid()));
        verify(systemImplSpy, timeout(5000)).go(SaleDetailView.VIEW_NAME, goArgs,
                PlatformTestUtil.getTargetContext());
    }

    @Test
    public void givenWhenPresenterCreated_whenHandleClickPrimaryCalled_shouldGoToNewSaleDetail(){

        Hashtable args = new Hashtable();
        SaleListPresenter presenter = new SaleListPresenter(PlatformTestUtil.getTargetContext(),
                args, mockView);
        presenter.handleClickPrimaryActionButton();

        Hashtable<String, String> goArgs = new Hashtable<>();
        verify(systemImplSpy, timeout(5000)).go(SaleDetailView.VIEW_NAME, goArgs,
                PlatformTestUtil.getTargetContext());
    }

}
