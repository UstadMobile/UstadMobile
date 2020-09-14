package com.ustadmobile.door

import com.github.aakira.napier.Napier
import com.ustadmobile.door.DoorConstants.HEADER_DBVERSION
import com.ustadmobile.door.DoorDatabaseRepository.Companion.STATUS_CONNECTED
import com.ustadmobile.door.sse.DoorEventListener
import com.ustadmobile.door.sse.DoorEventSource
import com.ustadmobile.door.sse.DoorServerSentEvent
import com.ustadmobile.door.util.systemTimeInMillis
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.min

/**
 * The client sync manager is responsible to trigger the DoorDatabaseSyncRepository to sync tables
 * that have been updated (locally or remotely).
 *
 */
class ClientSyncManager(val repo: DoorDatabaseSyncRepository, val dbVersion: Int,
                        val maxProcessors: Int = 5, initialConnectivityStatus: Int,
                        private val syncDaoSubscribePath: String): TableChangeListener {

    val updateCheckJob: AtomicRef<Job?> = atomic(null)

    //TOOD: replace this with copyonWriteListOf
    val pendingJobs: MutableList<Int> = mutableListOf()

    val channel = Channel<Int>(Channel.UNLIMITED)

    val eventSource: AtomicRef<DoorEventSource?> = atomic(null)

    val eventSourceLock = Mutex()

    var connectivityStatus: Int = initialConnectivityStatus
        set(value) {
            field = value
            GlobalScope.launch {
                eventSourceLock.withLock {
                    if(value == STATUS_CONNECTED) {
                        checkEndpointEventSource()
                    }else {
                        val eventSourceVal = eventSource.value
                        if(eventSourceVal != null) {
                            eventSourceVal.close()
                            eventSource.value = null
                        }
                    }
                }
            }

            takeIf { value == STATUS_CONNECTED }?.checkQueue()
        }

    fun CoroutineScope.launchProcessor(id: Int, channel: Channel<Int>) = launch {
        for(tableId in channel) {
            try {
                val startTime = systemTimeInMillis()
                val syncResults = repo.sync(listOf(tableId))
                repo.takeIf { syncResults.any { it.tableId == tableId && it.status == SyncResult.STATUS_SUCCESS} }
                        ?.updateTableSyncStatusLastSynced(tableId, startTime)
                checkQueue()
            }catch(e: Exception) {
                Napier.e("Exception syncing tableid $id", e)
            }
        }
    }

    init {
        GlobalScope.launch {
            repeat(maxProcessors) {
                launchProcessor(it, channel)
            }

            if(initialConnectivityStatus == STATUS_CONNECTED) {
                checkQueue()
                checkEndpointEventSource()
            }
        }

        repo.addTableChangeListener(this)
    }

    suspend fun checkEndpointEventSource() {
        eventSourceLock.withLock {
            if(eventSource.value != null)
                return

            eventSource.value = DoorEventSource("${repo.endpoint}$syncDaoSubscribePath?deviceId=${repo.clientId}&$HEADER_DBVERSION=$dbVersion",
                    object : DoorEventListener {
                override fun onOpen() {

                }

                override fun onMessage(message: DoorServerSentEvent) {
                    if(message.event.equals("update", true)) {
                        val lineParts = message.data.split(REGEX_WHITESPACE)
                        GlobalScope.launch {
                            repo.updateTableSyncStatusLastChanged(lineParts[0].toInt(),
                                    lineParts[1].toLong())
                            invalidate()
                        }
                    }
                }

                override fun onError(e: Exception) {

                }
            })
        }
    }


    override fun onTableChanged(tableName: String) {
        val tableId = repo.tableIdMap[tableName] ?: -1
        GlobalScope.launch {
            repo.updateTableSyncStatusLastChanged(tableId, systemTimeInMillis())
            invalidate()
        }
    }

    fun invalidate() {
        if(updateCheckJob.value == null) {
            updateCheckJob.value = GlobalScope.async {
                delay(300)
                updateCheckJob.value = null
                checkQueue()
            }
        }
    }

    fun checkQueue() {
        val newJobs = repo.findTablesToSync().filter { it.tsTableId !in pendingJobs }
        newJobs.subList(0, min(maxProcessors, newJobs.size)).forEach {
            pendingJobs += it.tsTableId
            channel.offer(it.tsTableId)
        }
    }

    fun close() {
        eventSource.getAndSet(null)?.also {
            it.close()
        }
    }

    companion object {

        val REGEX_WHITESPACE = "\\s+".toRegex()

    }

}