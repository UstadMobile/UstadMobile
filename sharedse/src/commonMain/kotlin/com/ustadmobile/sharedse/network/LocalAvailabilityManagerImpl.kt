package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.dao.LocallyAvailableContainerDao
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.networkmanager.AvailabilityMonitorRequest
import com.ustadmobile.core.networkmanager.LocalAvailabilityManager
import com.ustadmobile.lib.db.entities.EntryStatusResponse
import com.ustadmobile.lib.db.entities.LocallyAvailableContainer
import com.ustadmobile.lib.db.entities.NetworkNode
import com.ustadmobile.lib.db.entities.NetworkNodeWithStatusResponsesAndHistory
import com.ustadmobile.lib.util.copyOnWriteListOf
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.coroutines.*

typealias StatusTaskMakerFn = suspend (context: Any, containerUidsToCheck: List<Long>, networkNode: NetworkNode) -> BleEntryStatusTask?

typealias OnNodeStatusChangeFn = suspend (bluetoothAddr: String) -> Unit

class LocalAvailabilityManagerImpl(private val context: Any,
                                   private val entryStatusTaskMaker: StatusTaskMakerFn,
                                   private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
                                   private val onNewNodeDiscovered: OnNodeStatusChangeFn = { },
                                   private val onNodeLost: OnNodeStatusChangeFn = { },
                                   private val locallyAvailableContainerDao: LocallyAvailableContainerDao)
    : LocalAvailabilityManager{

    private val activeMonitoringRequests: MutableList<AvailabilityMonitorRequest> = copyOnWriteListOf()

    private val activeNodes: MutableList<NetworkNodeWithStatusResponsesAndHistory> = mutableListOf()

    private var dbAvailableContainerUids = mutableListOf<Long>()

    private var dbLastUpdated: Long = 0

    private var availablilityLastChanged: Long = 0

    suspend fun updateDb() {
        if(dbLastUpdated < availablilityLastChanged) {
            dbLastUpdated = getSystemTimeInMillis()
            val containersAvailableNow = activeNodes.flatMap {
                it.statusResponses.filter { it.value.available }.map { it.value.erContainerUid }
            }.toSet()
            val entriesLost = dbAvailableContainerUids.filter { it !in containersAvailableNow}
            val newEntries = containersAvailableNow.filter { it !in dbAvailableContainerUids }
            locallyAvailableContainerDao.takeIf { entriesLost.isNotEmpty() }
                    ?.deleteList(entriesLost.map { LocallyAvailableContainer(it) })
            locallyAvailableContainerDao.takeIf{newEntries.isNotEmpty() }
                    ?.insertList(newEntries.map { LocallyAvailableContainer(it) })
        }
    }

    override suspend fun handleNodeDiscovered(bluetoothAddr: String) {
        withContext(coroutineDispatcher) {
            val existingNode = activeNodes.firstOrNull { it.bluetoothMacAddress == bluetoothAddr }
            if(existingNode != null) {
                existingNode.lastUpdateTimeStamp = getSystemTimeInMillis()
            }else {
                UMLog.l(UMLog.INFO, 0, "AvailabilityManager: discovered new node: $bluetoothAddr")
                val networkNode = NetworkNodeWithStatusResponsesAndHistory()
                networkNode.bluetoothMacAddress = bluetoothAddr
                activeNodes.add(networkNode)
                val statusRequestUids = activeMonitoringRequests.flatMap { it.entryUidsToMonitor }.toSet()
                if(statusRequestUids.isNotEmpty()) {
                    sendRequest(networkNode, statusRequestUids.toList())
                }
                onNewNodeDiscovered(bluetoothAddr)
            }
        }
    }

    suspend fun sendRequest(networkNode: NetworkNode, containerUids: List<Long>) = withContext(coroutineDispatcher) {
        val statusTask = entryStatusTaskMaker.invoke(context, containerUids, networkNode)
        if(statusTask != null) {
            statusTask.statusResponseListener = this@LocalAvailabilityManagerImpl::handleBleTaskResponseReceived
            statusTask.sendRequest()
        }
    }

    fun handleBleTaskResponseReceived(entryStatusResponses: MutableList<EntryStatusResponse>, statusTask: BleEntryStatusTask) {
        GlobalScope.launch(coroutineDispatcher) {
            val networkNode = activeNodes.firstOrNull { it.bluetoothMacAddress == statusTask.networkNode.bluetoothMacAddress }
            if(networkNode == null)
                return@launch

            networkNode.statusResponses.putAll(entryStatusResponses.map { it.erContainerUid to it}.toMap())
            val entryStatusResponseContainerUids = entryStatusResponses.map { it.erContainerUid }
            activeMonitoringRequests.filter { monitorRequest ->
                monitorRequest.entryUidsToMonitor.any { it in entryStatusResponseContainerUids }
            }.forEach {
                val intersecting = it.entryUidsToMonitor.intersect(entryStatusResponseContainerUids)
                it.onEntityAvailabilityChanged(areContentEntriesLocallyAvailable(intersecting.toList()))
            }

            availablilityLastChanged = getSystemTimeInMillis()
            launch(coroutineDispatcher) {
                delay(1000)
                updateDb()
            }
        }
    }

    override fun addMonitoringRequest(request: AvailabilityMonitorRequest) {
        //compute what we don't know here
        activeMonitoringRequests.add(request)
        val allMonitoredUids = activeMonitoringRequests.flatMap { it.entryUidsToMonitor }.toSet()

        //provide an immediate callback to provide statuses as far as we know for this request
        GlobalScope.launch {
            val responsesNeeded = activeNodes.map { node -> node to allMonitoredUids.filter { !node.statusResponses.containsKey(it) }}
                    .toMap().filter { it.value.isNotEmpty() }
            responsesNeeded.forEach { responseNeeded ->
                sendRequest(responseNeeded.key, responseNeeded.value)
            }

            request.onEntityAvailabilityChanged(areContentEntriesLocallyAvailable(request.entryUidsToMonitor))
        }
    }

    override fun removeMonitoringRequest(request: AvailabilityMonitorRequest) {
        activeMonitoringRequests.remove(request)
    }

    override suspend fun areContentEntriesLocallyAvailable(containerUids: List<Long>) : Map<Long, Boolean> {
        return withContext(coroutineDispatcher) {
            containerUids.map { containerUid -> containerUid to activeNodes.any { node -> node.statusResponses[containerUid]?.available ?: false }}.toMap()
        }
    }

    override suspend fun findBestLocalNodeForContentEntryDownload(containerUid: Long): NetworkNode? = withContext(coroutineDispatcher){
        activeNodes.filter { node -> node.statusResponses[containerUid]?.available ?: false }.
                sortedBy { it.nodeFailures.size }.firstOrNull()
    }
}