package com.ustadmobile.door

import com.github.aakira.napier.Napier
import com.ustadmobile.door.util.systemTimeInMillis
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

/**
 * The client sync manager is responsible to trigger the DoorDatabaseSyncRepository to sync tables
 * that have been updated (locally or remotely).
 *
 */
class ClientSyncManager(val repo: DoorDatabaseSyncRepository, val maxProcessors: Int = 5, initialConnectivityStatus: Int): TableChangeListener {

    val updateCheckJob: AtomicRef<Job?> = atomic(null)

    //TOOD: replace this with copyonWriteListOf
    val pendingJobs: MutableList<Int> = mutableListOf()

    val channel = Channel<Int>(Channel.UNLIMITED)

    var connectivityStatus: Int = initialConnectivityStatus
        set(value) {
            field = value
            if(value == DoorDatabaseRepository.STATUS_CONNECTED) {
                checkQueue()
            }
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
        }

        repo.addTableChangeListener(this)
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
        repo.findTablesToSync().filter { it.tsTableId in pendingJobs }
                .subList(0, maxProcessors - pendingJobs.size).forEach {
                    pendingJobs += it.tsTableId
                    channel.offer(it.tsTableId)
                }
    }

}