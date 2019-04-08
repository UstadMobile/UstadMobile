package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SaleItemDetailView;
import com.ustadmobile.core.view.SelectSaleProductView;
import com.ustadmobile.core.controller.SelectSaleProductPresenter;
import com.ustadmobile.lib.db.entities.SaleItem;
import com.ustadmobile.lib.db.entities.SaleProduct;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Hashtable;

import static com.ustadmobile.core.util.SetUpUtil.addDummyData;

import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_PRODUCT_UID;
import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_UID;
import static com.ustadmobile.core.view.SelectProducerView.ARG_PRODUCER_UID;
import static com.ustadmobile.lib.db.entities.SaleProductGroup.PRODUCT_GROUP_TYPE_CATEGORY;
import static com.ustadmobile.test.core.util.CoreTestUtil.TEST_URI;
import static com.ustadmobile.test.core.util.CoreTestUtil.startServer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class TestSelectSaleProductPresenter {

    UstadMobileSystemImpl mainImpl;

    UstadMobileSystemImpl systemImplSpy;

    private HttpServer server;

    private UmAppDatabase db;

    private UmAppDatabase repo;

    private SelectSaleProductView mockView;


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

        mockView = Mockito.mock(SelectSaleProductView.class);
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
    public void givenWhenSaleItemAndProducerUidGiven_whenonCreated_shouldFillRecentAndCategoryProviders() throws InterruptedException {
        Hashtable<String, String> incomingArgs = new Hashtable<>();

        SaleItem saleItem = repo.getSaleItemDao().findAllActiveList().get(0);
        long saleItemUid = saleItem.getSaleItemUid();
        long producerUid = repo.getPersonDao().findAllPeople().get(1).getPersonUid();

        incomingArgs.put(ARG_SALE_ITEM_UID, String.valueOf(saleItemUid));
        incomingArgs.put(ARG_PRODUCER_UID, String.valueOf(producerUid));
        SelectSaleProductPresenter presenter = new SelectSaleProductPresenter(PlatformTestUtil.getTargetContext(),
                incomingArgs, mockView);
        presenter.onCreate(incomingArgs);
        Thread.sleep(1000);

        verify(mockView, timeout(5000)).setRecentProvider(
                repo.getSaleProductDao().findAllActiveSNWIProvider());

        verify(mockView, timeout(5000)).setCategoryProvider(
                repo.getSaleProductGroupDao().findAllTypedActiveSNWIProvider(PRODUCT_GROUP_TYPE_CATEGORY));

        Hashtable<String, String> outArgs = new Hashtable<>();

        //TODO: Test for collections provider as well

    }

    @Test
    public void givenWhenSaleItemAndProducerUidGiven_whenProductClicked_shouldGoToSaleItemDetaiView() throws InterruptedException {
        Hashtable<String, String> incomingArgs = new Hashtable<>();

        SaleItem saleItem = repo.getSaleItemDao().findAllActiveList().get(0);
        long saleItemUid = saleItem.getSaleItemUid();
        long producerUid = repo.getPersonDao().findAllPeople().get(1).getPersonUid();

        incomingArgs.put(ARG_SALE_ITEM_UID, String.valueOf(saleItemUid));
        incomingArgs.put(ARG_PRODUCER_UID, String.valueOf(producerUid));
        SelectSaleProductPresenter presenter = new SelectSaleProductPresenter(PlatformTestUtil.getTargetContext(),
                incomingArgs, mockView);
        presenter.onCreate(incomingArgs);
        Thread.sleep(1000);

        long productUid = repo.getSaleProductDao().findAllActiveList().get(0).getSaleProductUid();

        presenter.handleClickProduct(productUid);

        Hashtable<String, String> outArgs = new Hashtable<>();
        outArgs.put(ARG_SALE_ITEM_PRODUCT_UID, String.valueOf(productUid));
        outArgs.put(ARG_PRODUCER_UID, String.valueOf(producerUid));
        outArgs.put(ARG_SALE_ITEM_UID, String.valueOf(saleItemUid));

        verify(systemImplSpy, timeout(5000)).go(SaleItemDetailView.VIEW_NAME, outArgs,
                PlatformTestUtil.getTargetContext());

    }

}
