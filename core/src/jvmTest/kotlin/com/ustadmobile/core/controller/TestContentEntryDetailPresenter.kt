package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.ContentEntryListFragmentView
import com.ustadmobile.core.view.HomeView

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

import java.util.Hashtable

import com.ustadmobile.core.networkmanager.DownloadJobItemStatusProvider
import com.ustadmobile.core.controller.ContentEntryListFragmentPresenter.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.test.core.util.checkJndiSetup
import org.junit.Assert
import org.mockito.Mockito.spy

class TestContentEntryDetailPresenter {

    private var mockView: ContentEntryDetailView? = null
    private var monitor: LocalAvailabilityMonitor? = null
    private var statusProvider: DownloadJobItemStatusProvider? = null

    private val context = object: DoorLifecycleOwner {
        override fun addObserver(observer: DoorLifecycleObserver) {

        }

        override fun removeObserver(observer: DoorLifecycleObserver) {

        }

        override val currentState: Int
            get() = 1

    }

    @Before
    fun setUp() {
        checkJndiSetup()
        mockView = Mockito.mock(ContentEntryDetailView::class.java)
        monitor = spy(LocalAvailabilityMonitor::class.java)
        statusProvider = Mockito.mock(DownloadJobItemStatusProvider::class.java)
    }


    @Test
    fun givenUserIsInDetailViewFromListView_WhenUpNavigationCalled_thenShouldReturnToListView() {

        val args = Hashtable<String,String>()
        args.put(ARG_CONTENT_ENTRY_UID, 43L.toString())
        args.put(UstadMobileSystemCommon.ARG_REFERRER, REFERRER_FULL_PATH)

        val presenter = ContentEntryDetailPresenter(context,
                args, mockView!!, monitor!!, statusProvider!!)
        presenter.onCreate(args)

        val argsresult = Hashtable<String,String>()
        argsresult.put(ARG_CONTENT_ENTRY_UID, 42L.toString())

        presenter.handleUpNavigation()

        val lastGoToDest = UstadMobileSystemImpl.instance.lastDestination
        Assert.assertEquals("Last destination was ContentEntryListFragmentView",
                ContentEntryListFragmentView.VIEW_NAME, lastGoToDest!!.viewName)
        Assert.assertEquals("Last destination had expect content entry uid arg",
                "42", lastGoToDest.args[ARG_CONTENT_ENTRY_UID])
    }

    @Test
    fun givenUserIsInDetailViewFromNavigation_WhenUpNavigationCalled_thenShouldReturnToDummyView() {

        val args = Hashtable<String,String>()
        args.put(ARG_CONTENT_ENTRY_UID, 42L.toString())
        args.put(UstadMobileSystemCommon.ARG_REFERRER, REFERRER_NO_PATH)

        val presenter = ContentEntryDetailPresenter(context,
                args, mockView!!, monitor!!, statusProvider!!)
        presenter.onCreate(args)

        args.remove(UstadMobileSystemCommon.ARG_REFERRER)

        presenter.handleUpNavigation()

        val lastGoToDest = UstadMobileSystemImpl.instance.lastDestination
        Assert.assertEquals("Last destination view name is hoem view", HomeView.VIEW_NAME,
                lastGoToDest!!.viewName)
    }

    companion object {

        private val REFERRER_FULL_PATH = "/DummyView?/ContentEntryList?entryid=41/ContentEntryList?entryid=42/ContentEntryDetail?entryid=43"
        private val REFERRER_NO_PATH = ""
        private val flags = UstadMobileSystemCommon.GO_FLAG_CLEAR_TOP or UstadMobileSystemCommon.GO_FLAG_SINGLE_TOP
    }

}
