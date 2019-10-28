package com.ustadmobile.sharedse.network

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.networkmanager.AvailabilityMonitorRequest
import com.ustadmobile.core.networkmanager.LocalAvailabilityManager
import com.ustadmobile.lib.db.entities.DownloadJobItem
import org.junit.Assert
import org.junit.Test

class NetworkManagerBleCommonTest {

    @Test
    fun givenEmptyDownloadQueue_whenJobItemsAreQueued_thenShouldRequestLocalAvailability() {
        val localAvailabilityManager = mock<LocalAvailabilityManager>()
        val downloadQueueLocalAvailabilityMonitor = NetworkManagerBleCommon.DownloadQueueLocalAvailabilityObserver(
                localAvailabilityManager)

        val downloadJobItems = (0.. 9).map{ DownloadJobItem(0, 0L, it.toLong(), 0L) }
        downloadQueueLocalAvailabilityMonitor.onChanged(downloadJobItems)
        argumentCaptor<AvailabilityMonitorRequest>().apply {
            verify(localAvailabilityManager).addMonitoringRequest(capture())
            Assert.assertEquals("Monitor request submitted for items in queue",
                    (0.. 9).map { it.toLong() }, firstValue.entryUidsToMonitor)
        }
    }

    @Test
    fun givenExistingDownloadQueue_whenJobItemsAreQueued_thenShouldRemovePreviousRequestAndAddNewRequest() {
        val localAvailabilityManager = mock<LocalAvailabilityManager>()
        val downloadQueueLocalAvailabilityMonitor = NetworkManagerBleCommon.DownloadQueueLocalAvailabilityObserver(
                localAvailabilityManager)
        val initDownloadItems = (0.. 9).map{ DownloadJobItem(0, 0L, it.toLong(), 0L) }
        downloadQueueLocalAvailabilityMonitor.onChanged(initDownloadItems)

        val nextDownloadItems = (10.. 19).map{ DownloadJobItem(0, 0L, it.toLong(), 0L) }
        downloadQueueLocalAvailabilityMonitor.onChanged(nextDownloadItems)

        argumentCaptor<AvailabilityMonitorRequest>().apply {
            verify(localAvailabilityManager, times(2)).addMonitoringRequest(capture())
            Assert.assertEquals("Monitor request submitted for first items in queue",
                    (0.. 9).map { it.toLong() }, firstValue.entryUidsToMonitor)
            Assert.assertEquals("Monitor request submitted for second items in queue",
                    (10.. 19).map { it.toLong() }, secondValue.entryUidsToMonitor)
        }

        argumentCaptor<AvailabilityMonitorRequest>().apply {
            verify(localAvailabilityManager).removeMonitoringRequest(capture())
            Assert.assertEquals("First monitoring request was removed",
                    (0.. 9).map { it.toLong() }, firstValue.entryUidsToMonitor)
        }
    }

    @Test
    fun givenEmptyDownloadQueue_whenOnChangeCalled_thenNoRequestShouldBeSubmitted() {
        val localAvailabilityManager = mock<LocalAvailabilityManager>()
        val downloadQueueLocalAvailabilityMonitor = NetworkManagerBleCommon.DownloadQueueLocalAvailabilityObserver(
                localAvailabilityManager)

        downloadQueueLocalAvailabilityMonitor.onChanged(listOf())

        verify(localAvailabilityManager, never()).addMonitoringRequest(any())
    }

}