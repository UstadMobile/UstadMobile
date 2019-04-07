package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SaleDetailView;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static com.ustadmobile.core.util.SetUpUtil.addDummyData;

import static com.ustadmobile.test.core.util.CoreTestUtil.TEST_URI;
import static com.ustadmobile.test.core.util.CoreTestUtil.startServer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

public class TestSaleDetailPresenter {

    UstadMobileSystemImpl mainImpl;

    UstadMobileSystemImpl systemImplSpy;

    private HttpServer server;

    private UmAppDatabase db;

    private UmAppDatabase repo;

    private SaleDetailView mockView;


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

        mockView = Mockito.mock(SaleDetailView.class);
        doAnswer((invocationOnMock) -> {
            new Thread(((Runnable) invocationOnMock.getArgument(0))).start();
            return null;
        }).when(mockView).runOnUiThread(any());
    }

    @Test
    public void given_when_should() {

    }

}
