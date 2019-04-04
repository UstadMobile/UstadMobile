package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.Login2View;
import com.ustadmobile.core.view.SaleListView;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.ustadmobile.test.core.util.CoreTestUtil.TEST_URI;
import static com.ustadmobile.test.core.util.CoreTestUtil.startServer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

public class TestSaleListPresenter {

    UstadMobileSystemImpl mainImpl;

    UstadMobileSystemImpl systemImplSpy;

    private HttpServer server;

    private UmAppDatabase db;

    private UmAppDatabase repo;

    private static final String VALID_USER = "testuser";
    public static final String TEACHER_USER = "teachera";
    private static final String VALID_PASS = "secret";

    private long testClazzUid;

    List<ClazzMember> clazzMembers;

    private long teacherRoleUid = 0L;
    private long teacherPersonUid = 0L;
    private long teacherPersonGroupUid = 0L;

    private SaleListView mockView;
    private Login2View loginView;

    public static final int TARDY_CLAZZMEMBER_POSITION = 2;

    @Before
    public void setUp(){

        mainImpl = UstadMobileSystemImpl.getInstance();
        systemImplSpy = Mockito.spy(mainImpl);
        UstadMobileSystemImpl.setMainInstance(systemImplSpy);
        server = startServer();

        db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        repo = db.getRepository(TEST_URI, "");

        db.clearAllTables();


        mockView = Mockito.mock(SaleListView.class);
        doAnswer((invocationOnMock) -> {
            new Thread(((Runnable)invocationOnMock.getArgument(0))).start();
            return null;
        }).when(mockView).runOnUiThread(any());
    }

    @Test
    public void givenWhenSaleEntriesCreated_whenViewAndPresenterLoads_shouldReturnData(){

    }

}
