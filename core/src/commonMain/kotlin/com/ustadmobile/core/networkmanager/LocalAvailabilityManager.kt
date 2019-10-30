package com.ustadmobile.core.networkmanager

import com.ustadmobile.lib.db.entities.NetworkNode

class AvailabilityMonitorRequest(val entryUidsToMonitor: List<Long>,
                                 val onEntityAvailabilityChanged: (Map<Long, Boolean>) -> Unit = {})

interface LocalAvailabilityManager {

    suspend fun handleNodeDiscovered(bluetoothAddr: String)

    fun addMonitoringRequest(request: AvailabilityMonitorRequest)

    fun removeMonitoringRequest(request: AvailabilityMonitorRequest)

    suspend fun areContentEntriesLocallyAvailable(containerUids: List<Long>) : Map<Long, Boolean>

    suspend fun findBestLocalNodeForContentEntryDownload(containerUid: Long): NetworkNode?
}