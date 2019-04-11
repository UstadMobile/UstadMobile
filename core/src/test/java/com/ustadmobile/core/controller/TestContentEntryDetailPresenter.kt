package com.ustadmobile.core.controller

import com.ustadmobile.core.CoreTestConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.ContentEntryListFragmentView
import com.ustadmobile.core.view.DummyView
import com.ustadmobile.lib.database.jdbc.DriverConnectionPoolInitializer
import com.ustadmobile.test.core.impl.PlatformTestUtil

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

import java.util.Hashtable

import com.ustadmobile.core.controller.ContentEntryListPresenter.Companion.ARG_CONTENT_ENTRY_UID
import org.mockito.Mockito.spy
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify

class TestContentEntryDetailPresenter {

    internal var mainImpl: UstadMobileSystemImpl? = null
    internal var systemImplSpy: UstadMobileSystemImpl? = null
    private var mockView: ContentEntryDetailView? = null
    private var monitor: LocalAvailabilityMonitor? = null

    @Before
    fun setUp() {
        DriverConnectionPoolInitializer.bindDataSource("UmAppDatabase",
                CoreTestConfig.TESTDB_JDBCURL_UMMAPPDATABASE, true)
        mainImpl = UstadMobileSystemImpl.instance
        systemImplSpy = Mockito.spy(mainImpl)
        UstadMobileSystemImpl.setMainInstance(systemImplSpy)

        mockView = Mockito.mock(ContentEntryDetailView::class.java)
        monitor = spy(LocalAvailabilityMonitor::class.java)
    }

    @After
    fun tearDown() {
        UstadMobileSystemImpl.setMainInstance(mainImpl)
        systemImplSpy = null
    }


    @Test
    fun givenUserIsInDetailViewFromListView_WhenUpNavigationCalled_thenShouldReturnToListView() {

        val args = Hashtable<String,String>()
        args.put(ARG_CONTENT_ENTRY_UID, 43L.toString())
        args.put(UstadMobileSystemImpl.ARG_REFERRER, REFERRER_FULL_PATH)

        val presenter = ContentEntryDetailPresenter(PlatformTestUtil.targetContext,
                args, mockView!!, monitor!!)
        presenter.onCreate(args)

        val argsresult = Hashtable<String,String>()
        argsresult.put(ARG_CONTENT_ENTRY_UID, 42L.toString())

        presenter.handleUpNavigation()


        verify(systemImplSpy, timeout(5000))?.go(ContentEntryListFragmentView.VIEW_NAME, argsresult , PlatformTestUtil.targetContext, flags)

    }

    @Test
    fun givenUserIsInDetailViewFromNavigation_WhenUpNavigationCalled_thenShouldReturnToDummyView() {

        val args = Hashtable<String,String>()
        args.put(ARG_CONTENT_ENTRY_UID, 42L.toString())
        args.put(UstadMobileSystemImpl.ARG_REFERRER, REFERRER_NO_PATH)

        val presenter = ContentEntryDetailPresenter(PlatformTestUtil.targetContext,
                args, mockView!!, monitor!!)
        presenter.onCreate(args)

        args.remove(UstadMobileSystemImpl.ARG_REFERRER)

        presenter.handleUpNavigation()
        verify<UstadMobileSystemImpl>(systemImplSpy, timeout(5000)).go(DummyView.VIEW_NAME, mutableMapOf(),
                PlatformTestUtil.targetContext, UstadMobileSystemImpl.GO_FLAG_CLEAR_TOP or UstadMobileSystemImpl.GO_FLAG_SINGLE_TOP)

    }

    companion object {

        private val REFERRER_FULL_PATH = "/DummyView?/ContentEntryList?entryid=41/ContentEntryList?entryid=42/ContentEntryDetail?entryid=43"
        private val REFERRER_NO_PATH = ""
        private val flags = UstadMobileSystemImpl.GO_FLAG_CLEAR_TOP or UstadMobileSystemImpl.GO_FLAG_SINGLE_TOP
    }

}
