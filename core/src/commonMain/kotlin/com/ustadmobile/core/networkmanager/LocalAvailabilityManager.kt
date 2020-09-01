package com.ustadmobile.core.networkmanager

import com.ustadmobile.lib.db.entities.NetworkNode

class AvailabilityMonitorRequest(val containerUidsToMonitor: List<Long>,
                                 val onContainerAvailabilityChanged: (Map<Long, Boolean>) -> Unit = {})

interface LocalAvailabilityManager {

    suspend fun onNewNodeDiscovered(node: NetworkNode)

    suspend fun handleNodesLost(bluetoothAddrs: List<String>)

    fun addMonitoringRequest(request: AvailabilityMonitorRequest)

    fun removeMonitoringRequest(request: AvailabilityMonitorRequest)

    suspend fun areContentEntriesLocallyAvailable(containerUids: List<Long>) : Map<Long, Boolean>

    suspend fun findBestLocalNodeForContentEntryDownload(containerUid: Long): NetworkNode?
}