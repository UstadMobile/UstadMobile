package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SaleItemDetailView;
import com.ustadmobile.lib.db.entities.SaleItem;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Hashtable;

import static com.ustadmobile.core.util.SetUpUtil.addDummyData;
import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_PRODUCT_UID;
import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_UID;
import static com.ustadmobile.core.view.SelectProducerView.ARG_PRODUCER_UID;
import static com.ustadmobile.test.core.util.CoreTestUtil.TEST_URI;
import static com.ustadmobile.test.core.util.CoreTestUtil.startServer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class TestSaleItemDetailPresenter {

    UstadMobileSystemImpl mainImpl;

    UstadMobileSystemImpl systemImplSpy;

    private HttpServer server;

    private UmAppDatabase db;

    private UmAppDatabase repo;

    private SaleItemDetailView mockView;

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

        mockView = Mockito.mock(SaleItemDetailView.class);
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
    public void givenSaleItemOnly_whenOnCreateRun_shouldUpdateViewWithSaleItem() throws InterruptedException {
        long saleItemUid = repo.getSaleItemDao().findAllActiveList().get(0).getSaleItemUid();
        int beforeSaleItemActiveCount = repo.getSaleItemDao().findAllActiveList().size();
        int beforeSaleItemAllCount = repo.getSaleItemDao().findAllList().size();
        Hashtable<String, String> incomingArgs = new Hashtable<>();
        incomingArgs.put(ARG_SALE_ITEM_UID, String.valueOf(saleItemUid));
        SaleItemDetailPresenter presenter =
                new SaleItemDetailPresenter(PlatformTestUtil.getTargetContext(),
                incomingArgs, mockView);
        presenter.onCreate(incomingArgs);
        Thread.sleep(1000);

        int afterSaleItemActiveCount = repo.getSaleItemDao().findAllActiveList().size();
        int afterSaleItemAllCount = repo.getSaleItemDao().findAllList().size();

        Assert.assertEquals(beforeSaleItemActiveCount,afterSaleItemActiveCount );
        Assert.assertEquals(beforeSaleItemAllCount,afterSaleItemAllCount );
        verify(mockView, timeout(1000)).updateSaleItemOnView(any(SaleItem.class));

    }

    @Test
    public void givenSaleItemAndProducerProduct_whenSaved_shouldPersist() throws InterruptedException {

        SaleItem saleItem = repo.getSaleItemDao().findAllActiveList().get(0);
        long saleItemUid = saleItem.getSaleItemUid();
        long productUid = repo.getSaleProductDao().findAllActiveList().get(1).getSaleProductUid();
        long producerUid = repo.getPersonDao().findAllPeople().get(1).getPersonUid();

        int beforeSaleItemActiveCount = repo.getSaleItemDao().findAllActiveList().size();
        int beforeSaleItemAllCount = repo.getSaleItemDao().findAllList().size();

        Hashtable<String, String> incomingArgs = new Hashtable<>();
        incomingArgs.put(ARG_SALE_ITEM_UID, String.valueOf(saleItemUid));
        incomingArgs.put(ARG_PRODUCER_UID, String.valueOf(producerUid));
        incomingArgs.put(ARG_SALE_ITEM_PRODUCT_UID, String.valueOf(productUid));

        SaleItemDetailPresenter presenter =
                new SaleItemDetailPresenter(PlatformTestUtil.getTargetContext(),
                        incomingArgs, mockView);
        presenter.onCreate(incomingArgs);
        Thread.sleep(1000);
        presenter.handleClickSave();
        Thread.sleep(1000);
        SaleItem saleItemAfterPersist = repo.getSaleItemDao().findByUid(saleItemUid);

        Assert.assertEquals(saleItemAfterPersist.getSaleItemProducerUid(), producerUid);
        Assert.assertEquals(saleItemAfterPersist.getSaleItemProductUid(), productUid);
        Assert.assertNotEquals(saleItem.getSaleItemProductUid(), productUid);
        Assert.assertNotEquals(saleItem.getSaleItemProducerUid(), producerUid);


    }

    @Test
    public void givenSaleItem_whenSoldAndQuantityAndPPPChangedAndSaved_shouldPersist() throws InterruptedException {
        SaleItem saleItem = repo.getSaleItemDao().findAllActiveList().get(0);
        long saleItemUid = saleItem.getSaleItemUid();
        long productUid = repo.getSaleProductDao().findAllActiveList().get(1).getSaleProductUid();
        long producerUid = repo.getPersonDao().findAllPeople().get(1).getPersonUid();

        int beforeSaleItemActiveCount = repo.getSaleItemDao().findAllActiveList().size();
        int beforeSaleItemAllCount = repo.getSaleItemDao().findAllList().size();

        Hashtable<String, String> incomingArgs = new Hashtable<>();
        incomingArgs.put(ARG_SALE_ITEM_UID, String.valueOf(saleItemUid));
        incomingArgs.put(ARG_PRODUCER_UID, String.valueOf(producerUid));
        incomingArgs.put(ARG_SALE_ITEM_PRODUCT_UID, String.valueOf(productUid));

        SaleItemDetailPresenter presenter =
                new SaleItemDetailPresenter(PlatformTestUtil.getTargetContext(),
                        incomingArgs, mockView);
        presenter.onCreate(incomingArgs);
        Thread.sleep(1000);

        presenter.handleChangePPP(420);
        presenter.handleChangeQuantity(42);
        presenter.setSold(true);
        presenter.handleClickSave();

        Thread.sleep(1000);
        SaleItem saleItemPost = repo.getSaleItemDao().findByUid(saleItemUid);

        Assert.assertNotEquals(saleItem.getSaleItemQuantity(), 42);
        Assert.assertNotEquals(saleItem.getSaleItemPricePerPiece(), 420);
        Assert.assertNotEquals(saleItem.isSaleItemSold(), true);
        Assert.assertEquals(saleItemPost.getSaleItemQuantity(), 42);
        if(saleItemPost.getSaleItemPricePerPiece() == 420L){
            Assert.assertTrue(true);
        }else{
            Assert.assertTrue(false);
        }
        Assert.assertEquals(saleItemPost.isSaleItemSold(), true);


        Hashtable<String, String> outArgs = new Hashtable<>();

    }

}
