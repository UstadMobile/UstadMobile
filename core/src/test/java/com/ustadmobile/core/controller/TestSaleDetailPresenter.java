package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.SaleDao;
import com.ustadmobile.core.db.dao.SaleItemDao;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SaleDetailView;
import com.ustadmobile.core.controller.SaleDetailPresenter;
import com.ustadmobile.core.view.SaleItemDetailView;
import com.ustadmobile.lib.db.entities.Sale;
import com.ustadmobile.lib.db.entities.SaleItemListDetail;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Hashtable;

import static com.ustadmobile.core.util.SetUpUtil.addDummyData;

import static com.ustadmobile.core.view.SaleDetailView.ARG_SALE_UID;
import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_UID;
import static com.ustadmobile.test.core.util.CoreTestUtil.TEST_URI;
import static com.ustadmobile.test.core.util.CoreTestUtil.startServer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class TestSaleDetailPresenter {

    UstadMobileSystemImpl mainImpl;

    UstadMobileSystemImpl systemImplSpy;

    private HttpServer server;

    private UmAppDatabase db;

    private UmAppDatabase repo;

    private SaleDetailView mockView;

    private SaleDao saleDao;


    @Before
    public void setUp() {

        mainImpl = UstadMobileSystemImpl.getInstance();
        systemImplSpy = Mockito.spy(mainImpl);
        UstadMobileSystemImpl.setMainInstance(systemImplSpy);
        server = startServer();

        db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        repo = db.getRepository(TEST_URI, "");

        db.clearAllTables();

        addDummyData(repo);

        saleDao = repo.getSaleDao();

        mockView = Mockito.mock(SaleDetailView.class);
        doAnswer((invocationOnMock) -> {
            new Thread(((Runnable) invocationOnMock.getArgument(0))).start();
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
    public void givenWhenNoArgGiven_whenOnCreateRun_shouldCreateNewSaleAndUpdateViewAndLoc() throws InterruptedException {
        Hashtable<String, String> incomingArgs = new Hashtable<>();

        int beforeSaleCount = saleDao.findAllActiveList().size();
        SaleDetailPresenter presenter = new SaleDetailPresenter(PlatformTestUtil.getTargetContext(),
                incomingArgs, mockView);
        presenter.onCreate(incomingArgs);
        Thread.sleep(2000);
        int afterSaleActiveCount = saleDao.findAllActiveList().size();
        int afterSaleAllCount = saleDao.findAllList().size();

        Assert.assertEquals(beforeSaleCount, afterSaleActiveCount);
        Assert.assertEquals(afterSaleAllCount, beforeSaleCount+1);

        //Assert view is updated.
        verify(mockView, timeout(5000)).updateSaleOnView(any(Sale.class));


    }

    @Test
    public void givenWhenSaleUidArgGiven_whenOnCreateRun_shouldNotCreateNewSaleAndUpdateViewAndLoc() throws InterruptedException {
        Hashtable<String, String> incomingArgs = new Hashtable<>();
        long saleUid = saleDao.findAllActiveList().get(0).getSaleUid();
        incomingArgs.put(ARG_SALE_UID, String.valueOf(saleUid));

        int beforeSaleCount = saleDao.findAllActiveList().size();

        SaleDetailPresenter presenter = new SaleDetailPresenter(PlatformTestUtil.getTargetContext(),
                incomingArgs, mockView);
        presenter.onCreate(incomingArgs);

        Thread.sleep(2000);
        int afterSaleActiveCount = saleDao.findAllActiveList().size();
        int afterSaleAllCount = saleDao.findAllList().size();


        Assert.assertEquals(beforeSaleCount, afterSaleActiveCount);
        Assert.assertEquals(beforeSaleCount, afterSaleAllCount);

        //Assert view is updated.
        verify(mockView, timeout(5000)).updateSaleOnView(any(Sale.class));
    }

    @Test
    public void givenWhenSaleUidArgGiven_whenOnCreateRun_shouldLoadSaleItemProviderOnView() {
        Hashtable<String, String> incomingArgs = new Hashtable<>();
        long saleUid = saleDao.findAllActiveList().get(0).getSaleUid();
        incomingArgs.put(ARG_SALE_UID, String.valueOf(saleUid));

        SaleDetailPresenter presenter = new SaleDetailPresenter(PlatformTestUtil.getTargetContext(),
                incomingArgs, mockView);
        presenter.onCreate(incomingArgs);

        SaleItemDao saleItemDao = repo.getSaleItemDao();
        UmProvider<SaleItemListDetail> provider = saleItemDao.findAllSaleItemListDetailActiveBySaleProvider(saleUid);
        verify(mockView, timeout(5000)).setListProvider(provider);


    }

    @Test
    public void givenWhenSaleDetailLoaded_whenDeliveredChanged_shouldCallMethodAndPersist() throws InterruptedException {
        Hashtable<String, String> incomingArgs = new Hashtable<>();
        long saleUid = saleDao.findAllActiveList().get(0).getSaleUid();
        incomingArgs.put(ARG_SALE_UID, String.valueOf(saleUid));

        Sale beforeDeliveryUpdate = saleDao.findByUid(saleUid);

        SaleDetailPresenter presenter = new SaleDetailPresenter(PlatformTestUtil.getTargetContext(),
                incomingArgs, mockView);
        presenter.onCreate(incomingArgs);
        Thread.sleep(1000);

        presenter.handleSetDelivered(!beforeDeliveryUpdate.isSaleDone());

        presenter.handleClickSave();

        Thread.sleep(1000);

        Sale afterDeliveryUpdate = saleDao.findByUid(saleUid);

        Assert.assertEquals(beforeDeliveryUpdate.isSaleDone(), !afterDeliveryUpdate.isSaleDone());
        Hashtable<String, String> outArgs = new Hashtable<>();

    }

    @Test
    public void givenWhenSaleDetailLoaded_whenSaleItemClicked_shouldGoToSaleItemDetail() throws InterruptedException {
        Hashtable<String, String> incomingArgs = new Hashtable<>();
        long saleUid = saleDao.findAllActiveList().get(0).getSaleUid();
        incomingArgs.put(ARG_SALE_UID, String.valueOf(saleUid));

        SaleDetailPresenter presenter = new SaleDetailPresenter(PlatformTestUtil.getTargetContext(),
                incomingArgs, mockView);
        presenter.onCreate(incomingArgs);
        Thread.sleep(1000);


        long saleItemUid = repo.getSaleItemDao().findAllActiveList().get(0).getSaleItemUid();

        presenter.handleClickSaleItemEdit(saleItemUid);

        Hashtable<String, String> outArgs = new Hashtable<>();
        outArgs.put(ARG_SALE_ITEM_UID, String.valueOf(saleItemUid));
        verify(systemImplSpy, timeout(5000)).go(SaleItemDetailView.VIEW_NAME, outArgs,
                PlatformTestUtil.getTargetContext());

    }

    @Test
    public void givenWhenSaleDetailLoaded_whenAddSaleItemClicked_shouldGoToSelectProducerAndPersist() throws InterruptedException {
        Hashtable<String, String> incomingArgs = new Hashtable<>();
        long saleUid = saleDao.findAllActiveList().get(0).getSaleUid();
        incomingArgs.put(ARG_SALE_UID, String.valueOf(saleUid));

        SaleDetailPresenter presenter = new SaleDetailPresenter(PlatformTestUtil.getTargetContext(),
                incomingArgs, mockView);
        presenter.onCreate(incomingArgs);
        Thread.sleep(1000);

        //Get total sale items
        int beforeAddingSaleItemCount = repo.getSaleItemDao().findAllActiveList().size();

        presenter.handleClickAddSaleItem();

        Thread.sleep(1000);
        int afterAddingSaleItemCount = repo.getSaleItemDao().findAllActiveList().size();
        int afterAddingIncInactiveCount = repo.getSaleItemDao().findAllList().size();

        Assert.assertEquals(beforeAddingSaleItemCount, afterAddingSaleItemCount);
        Assert.assertEquals(beforeAddingSaleItemCount+1, afterAddingIncInactiveCount);

    }

    @Test
    public void givenWhenSaleDetailLoaded_whenDiscountChanged_shouldUpdateTotal() throws InterruptedException {
        Hashtable<String, String> incomingArgs = new Hashtable<>();
        Sale sale = saleDao.findAllActiveList().get(0);
        long saleUid = sale.getSaleUid();
        long oldDiscount = sale.getSaleDiscount();
        incomingArgs.put(ARG_SALE_UID, String.valueOf(saleUid));

        SaleDetailPresenter presenter = new SaleDetailPresenter(PlatformTestUtil.getTargetContext(),
                incomingArgs, mockView);
        presenter.onCreate(incomingArgs);
        Thread.sleep(1000);

        presenter.handleDiscountChanged(oldDiscount+25);

        verify(mockView, timeout(1000)).updateOrderTotalAfterDiscount(oldDiscount+25);

        presenter.handleClickSave();
        Thread.sleep(1000);
        long newDiscount = saleDao.findByUid(saleUid).getSaleDiscount();

        Assert.assertEquals(oldDiscount+25, newDiscount);

    }
}
