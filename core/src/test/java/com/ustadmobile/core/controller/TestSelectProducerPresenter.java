package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SaleItemDetailView;
import com.ustadmobile.core.view.SelectProducerView;
import com.ustadmobile.core.controller.SelectProducerPresenter;
import com.ustadmobile.core.view.SelectSaleProductView;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Hashtable;

import static com.ustadmobile.core.util.SetUpUtil.addDummyData;

import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_UID;
import static com.ustadmobile.core.view.SelectProducerView.ARG_PRODUCER_UID;
import static com.ustadmobile.test.core.util.CoreTestUtil.TEST_URI;
import static com.ustadmobile.test.core.util.CoreTestUtil.startServer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class TestSelectProducerPresenter {

    UstadMobileSystemImpl mainImpl;

    UstadMobileSystemImpl systemImplSpy;

    private HttpServer server;

    private UmAppDatabase db;

    private UmAppDatabase repo;

    private SelectProducerView mockView;


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

        mockView = Mockito.mock(SelectProducerView.class);
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
    public void givenSaleItemUid_whenProducerClicked_shouldGoToSelectSaleAndPassSaleItemAndProducer() throws InterruptedException {
        Hashtable<String, String> incomingArgs = new Hashtable<>();

        long saleItemUid = repo.getSaleItemDao().findAllActiveList().get(0).getSaleItemUid();
        incomingArgs.put(ARG_SALE_ITEM_UID, String.valueOf(saleItemUid));
        SelectProducerPresenter presenter = new SelectProducerPresenter(
                PlatformTestUtil.getTargetContext(),incomingArgs, mockView);

        long personUid = repo.getPersonDao().findAllPeople().get(0).getPersonUid();
        presenter.onCreate(incomingArgs);
        Thread.sleep(1000);

        presenter.handleClickProducer(personUid);

        Hashtable<String, String> outArgs = new Hashtable<>();
        outArgs.put(ARG_SALE_ITEM_UID, String.valueOf(saleItemUid));
        outArgs.put(ARG_PRODUCER_UID, String.valueOf(personUid));
        verify(systemImplSpy, timeout(5000)).go(SelectSaleProductView.VIEW_NAME, outArgs,
                PlatformTestUtil.getTargetContext());

    }

    @Test
    public void givenWhenSaleItemUid_whenOnCreated_shouldLoadProviderOnView() throws InterruptedException {

        Hashtable<String, String> incomingArgs = new Hashtable<>();

        long saleItemUid = repo.getSaleItemDao().findAllActiveList().get(0).getSaleItemUid();
        incomingArgs.put(ARG_SALE_ITEM_UID, String.valueOf(saleItemUid));
        SelectProducerPresenter presenter = new SelectProducerPresenter(
                PlatformTestUtil.getTargetContext(),incomingArgs, mockView);

        long personUid = repo.getPersonDao().findAllPeople().get(0).getPersonUid();
        presenter.onCreate(incomingArgs);
        Thread.sleep(1000);

        verify(mockView, timeout(5000)).setListProvider(
                repo.getPersonDao().findAllPeopleProvider());

    }

}
