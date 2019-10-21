package com.ustadmobile.port.android.view

import androidx.paging.PagedList
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.ustadmobile.core.networkmanager.AvailabilityMonitorRequest
import com.ustadmobile.core.networkmanager.LocalAvailabilityManager
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainerUid
import org.junit.Assert
import org.junit.Test

class TestContentEntryListFragment {

    //Tests for LocalAvailabilityPagedListCallback
    @Test
    fun givenNewPagedItemList_whenOnChangedCalled_thenShouldSubmitNewAvailabilityRequest() {
        val mockAvailabilityManager = mock<LocalAvailabilityManager>()
        val pagedListConfig = PagedList.Config.Builder().setPageSize(20)
                .setPrefetchDistance(20).build()
        val contentList = (0 .. 99).map {
            val contentEntry = ContentEntryWithParentChildJoinAndStatusAndMostRecentContainerUid()
            contentEntry.mostRecentContainer = it.toLong()
        }

        val mockPagedList = mock<PagedList<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainerUid>> {
            on { config }.thenReturn(pagedListConfig)
            on {lastKey}.thenReturn(0)
            on {get(com.nhaarman.mockitokotlin2.any()) }.thenAnswer {invocation -> {
                contentList[invocation.getArgument<Int>(0)]
            }}
        }

        val callback = ContentEntryListFragment.LocalAvailabilityPagedListCallback(mockAvailabilityManager,
                mockPagedList, {})
        callback.onChanged(0, 20)

        argumentCaptor<AvailabilityMonitorRequest>().apply {
            verify(mockAvailabilityManager).addMonitoringRequest(capture())
            Assert.assertEquals("Availability monitor is requested for first 20 entries",
                    (0 .. 19).map { it.toLong() }, firstValue.entryUidsToMonitor)
        }
    }

    fun givenPagedItemList_whenOnChangeCalled_thenOldRequestIsRemoved() {

    }

}