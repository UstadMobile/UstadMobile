package com.ustadmobile.port.android.view

import androidx.paging.PagedList
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.networkmanager.AvailabilityMonitorRequest
import com.ustadmobile.core.networkmanager.LocalAvailabilityManager
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class TestContentEntryListFragment {

    private val pagedListConfig = PagedList.Config.Builder().setPageSize(20)
            .setPrefetchDistance(20).build()

    private val mockContentList = (0 .. 99).map {index ->
        val contentEntry = ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer()
        contentEntry.mostRecentContainer = Container().also { it.containerUid = index.toLong()}
        contentEntry.leaf = true
        contentEntry
    }

    //Tests for LocalAvailabilityPagedListCallback
    @Test
    fun givenNewPagedItemList_whenOnChangedCalled_thenShouldSubmitNewAvailabilityRequest() {
        val mockAvailabilityManager = mock<LocalAvailabilityManager>()



        val mockPagedList = mock<PagedList<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>> {
            on { config }.thenReturn(pagedListConfig)
            on { lastKey }.thenReturn(0)
            on { size }.thenAnswer { mockContentList.size }
            on {get(com.nhaarman.mockitokotlin2.any()) }.thenAnswer {invocation ->
                mockContentList[invocation.getArgument<Int>(0)]
            }
        }

        val callback = ContentEntryListFragment.LocalAvailabilityPagedListCallback(
                mockAvailabilityManager, mockPagedList, {})
        callback.onChanged(0, 20)

        argumentCaptor<AvailabilityMonitorRequest>().apply {
            verify(mockAvailabilityManager).addMonitoringRequest(capture())
            Assert.assertEquals("Availability monitor is requested for first 20 entries",
                    (0 .. 19).map { it.toLong() }, firstValue.entryUidsToMonitor)
        }
    }

    @Test
    fun givenPagedItemList_whenOnChangeCalled_thenOldRequestIsRemoved() {
        val mockAvailabilityManager = mock<LocalAvailabilityManager>()
        val lastKeyRef = AtomicInteger(0)

        val mockPagedList = mock<PagedList<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>> {
            on { config }.thenReturn(pagedListConfig)
            on { lastKey }.thenAnswer { lastKeyRef.get() }
            on { size }.thenAnswer { mockContentList.size }
            on {get(com.nhaarman.mockitokotlin2.any()) }.thenAnswer {invocation ->
                mockContentList[invocation.getArgument<Int>(0)]
            }
        }

        val callback = ContentEntryListFragment.LocalAvailabilityPagedListCallback(
                mockAvailabilityManager, mockPagedList, {})
        callback.onChanged(0, 20)
        lastKeyRef.set(20)
        callback.onChanged(0, 40)

        argumentCaptor<AvailabilityMonitorRequest>().apply {
            verify(mockAvailabilityManager).removeMonitoringRequest(capture())
            Assert.assertEquals("Availability monitor is removed after the second callback",
                    (0 .. 19).map { it.toLong() }, firstValue.entryUidsToMonitor)
        }

        argumentCaptor<AvailabilityMonitorRequest>().apply {
            verify(mockAvailabilityManager, times(2)).addMonitoringRequest(capture())
            Assert.assertEquals("First availability monitor is items 0 to 19",
                    (0 .. 19).map {it.toLong()}, firstValue.entryUidsToMonitor)
            Assert.assertEquals("Second availability monitor is items 0 to 39",
                    (0 .. 39).map {it.toLong()}, secondValue.entryUidsToMonitor)
        }
    }

}