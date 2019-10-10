package com.ustadmobile.sharedse.network

import com.ustadmobile.lib.db.entities.EntryStatusResponse
import com.ustadmobile.lib.db.entities.NetworkNode
import com.ustadmobile.lib.db.entities.NetworkNodeWithStatusResponsesAndHistory
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.coroutines.*

typealias StatusTaskMakerFn = suspend (context: Any, containerUidsToCheck: List<Long>, networkNode: NetworkNode) -> BleEntryStatusTask

class LocalAvailabilityManagerImpl(private val context: Any,
                                   private val entryStatusTaskMaker: StatusTaskMakerFn,
                                   private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default) {

    class AvailabilityMonitorRequest(val entryUidsToMonitor: List<Long>,
                                     val onEntityAvailabilityChanged: (Map<Long, Boolean>) -> Unit = {})

    private val activeMonitoringRequests: MutableList<AvailabilityMonitorRequest> = mutableListOf()

    private val activeNodes: MutableList<NetworkNodeWithStatusResponsesAndHistory> = mutableListOf()

    suspend fun handleNodeDiscovered(bluetoothAddr: String) {
        withContext(coroutineDispatcher) {
            val existingNode = activeNodes.firstOrNull { it.bluetoothMacAddress == bluetoothAddr }
            if(existingNode != null) {
                existingNode.lastUpdateTimeStamp = getSystemTimeInMillis()
            }else {
                val networkNode = NetworkNodeWithStatusResponsesAndHistory()
                networkNode.bluetoothMacAddress = bluetoothAddr
                activeNodes.add(networkNode)
                val statusRequestUids = activeMonitoringRequests.flatMap { it.entryUidsToMonitor }.toSet()
                if(statusRequestUids.isNotEmpty()) {
                    sendRequest(networkNode, statusRequestUids.toList())
                }
            }
        }
    }

    fun sendRequest(networkNode: NetworkNode, containerUids: List<Long>) {
        GlobalScope.launch {
            val statusTask = entryStatusTaskMaker.invoke(context, containerUids, networkNode)
            statusTask.statusResponseListener = this@LocalAvailabilityManagerImpl::handleBleTaskResponseReceived
            statusTask.sendRequest()
        }
    }

    fun handleBleTaskResponseReceived(entryStatusResponses: MutableList<EntryStatusResponse>, statusTask: BleEntryStatusTask) {
        GlobalScope.launch {
            withContext(coroutineDispatcher) {
                val networkNode = activeNodes.firstOrNull { it.bluetoothMacAddress == statusTask.networkNode.bluetoothMacAddress }
                if(networkNode == null)
                    return@withContext

                networkNode.statusResponses.putAll(entryStatusResponses.map { it.erContainerUid to it}.toMap())
                val entryStatusResponseContainerUids = entryStatusResponses.map { it.erContainerUid }
                activeMonitoringRequests.filter { monitorRequest ->
                    monitorRequest.entryUidsToMonitor.any { it in entryStatusResponseContainerUids }
                }.forEach {
//                    val intersecting = it.entryUidsToMonitor.intersect(entryStatusResponseContainerUids)
//                    it.onEntityAvailabilityChanged(intersecting.map { it to areContentEntriesLocallyAvailable(intersecting.toList())}.toMap())
                }
            }
        }


    }

    fun addMonitoringRequest(request: AvailabilityMonitorRequest) {
        //compute what we don't know here
        activeMonitoringRequests.add(request)
        val allMonitoredUids = activeMonitoringRequests.flatMap { it.entryUidsToMonitor }.toSet()
        val responsesNeeded = activeNodes.map { node -> node to allMonitoredUids.filter { !node.statusResponses.containsKey(it) }}
                .toMap().filter { it.value.isNotEmpty() }
        responsesNeeded.forEach { responseNeeded ->
            sendRequest(responseNeeded.key, responseNeeded.value)
        }
    }

    fun removeMonitoringRequest(request: AvailabilityMonitorRequest) {
        activeMonitoringRequests.remove(request)
    }

    suspend fun areContentEntriesLocallyAvailable(containerUids: List<Long>) : Map<Long, Boolean> {
        return withContext(coroutineDispatcher) {
            containerUids.map { containerUid -> containerUid to activeNodes.any { node -> node.statusResponses[containerUid]?.available ?: false }}.toMap()
        }
    }

    suspend fun findBestLocalNodeForContentEntryDownload(containerUid: Long): NetworkNode? {
        return activeNodes.filter { node -> node.statusResponses[containerUid]?.available ?: false }.
                sortedBy { it.nodeFailures.size }.firstOrNull()
    }

}