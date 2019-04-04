package com.ustadmobile.core.controller;

import com.ustadmobile.core.CoreTestConfig;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor;
import com.ustadmobile.core.view.ContentEntryDetailView;
import com.ustadmobile.core.view.ContentEntryListView;
import com.ustadmobile.core.view.DummyView;
import com.ustadmobile.lib.database.jdbc.DriverConnectionPoolInitializer;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Hashtable;

import static com.ustadmobile.core.controller.ContentEntryListPresenter.ARG_CONTENT_ENTRY_UID;
import static com.ustadmobile.core.impl.UstadMobileSystemImpl.ARG_REFERRER;
import static com.ustadmobile.core.impl.UstadMobileSystemImpl.GO_FLAG_CLEAR_TOP;
import static com.ustadmobile.core.impl.UstadMobileSystemImpl.GO_FLAG_SINGLE_TOP;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class TestContentEntryDetailPresenter {

    UstadMobileSystemImpl mainImpl;
    UstadMobileSystemImpl systemImplSpy;

    private static final String REFERRER_FULL_PATH = "/DummyView?/ContentEntryList?entryid=41/ContentEntryList?entryid=42/ContentEntryDetail?entryid=43";
    private static final String REFERRER_NO_PATH = "";
    private ContentEntryDetailView mockView;
    private LocalAvailabilityMonitor monitor;
    private static final int flags = GO_FLAG_CLEAR_TOP | GO_FLAG_SINGLE_TOP;

    @Before
    public void setUp() {
        DriverConnectionPoolInitializer.bindDataSource("UmAppDatabase",
                CoreTestConfig.TESTDB_JDBCURL_UMMAPPDATABASE, true);
        mainImpl = UstadMobileSystemImpl.getInstance();
        systemImplSpy = Mockito.spy(mainImpl);
        UstadMobileSystemImpl.setMainInstance(systemImplSpy);

        mockView = Mockito.mock(ContentEntryDetailView.class);
        monitor = spy(LocalAvailabilityMonitor.class);
    }

    @After
    public void tearDown() {
        UstadMobileSystemImpl.setMainInstance(mainImpl);
        systemImplSpy = null;
    }


    @Test
    public void givenUserIsInDetailViewFromListView_WhenUpNavigationCalled_thenShouldReturnToListView() {

        Hashtable args = new Hashtable();
        args.put(ARG_CONTENT_ENTRY_UID, String.valueOf(43L));
        args.put(ARG_REFERRER, REFERRER_FULL_PATH);

        ContentEntryDetailPresenter presenter = new ContentEntryDetailPresenter(PlatformTestUtil.getTargetContext(),
                args, mockView,monitor);
        presenter.onCreate(args);

        Hashtable argsresult = new Hashtable();
        argsresult.put(ARG_CONTENT_ENTRY_UID, String.valueOf(42L));

        presenter.handleUpNavigation();
        verify(systemImplSpy, timeout(5000)).go(ContentEntryListView.VIEW_NAME, argsresult,
                mockView.getContext(), flags);

    }

    @Test
    public void givenUserIsInDetailViewFromNavigation_WhenUpNavigationCalled_thenShouldReturnToDummyView() {

        Hashtable args = new Hashtable();
        args.put(ARG_CONTENT_ENTRY_UID, String.valueOf(42L));
        args.put(ARG_REFERRER, REFERRER_NO_PATH);

        ContentEntryDetailPresenter presenter = new ContentEntryDetailPresenter(PlatformTestUtil.getTargetContext(),
                args, mockView , monitor);
        presenter.onCreate(args);

        args.remove(ARG_REFERRER);

        presenter.handleUpNavigation();
        verify(systemImplSpy, timeout(5000)).go(DummyView.VIEW_NAME, null,
                mockView.getContext(), GO_FLAG_CLEAR_TOP | GO_FLAG_SINGLE_TOP);

    }

}
