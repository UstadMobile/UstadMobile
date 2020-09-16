package com.ustadmobile.door

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.atomic.AtomicReference

/**
 * This class is used on the primary server to listen for changes to the database and tell the
 * server side repository to generate the required UpdateNotification entities (using the
 * dispatchUpdateNotifications function).
 *
 * When a change is received from the database it will wait up to 100ms before triggering the
 * the repo's dispatchUpdateNotifications function (so that it can batch updates together when
 * bulk changes take place). The dispatchUpdateNotifications function is called using a coroutine
 * fan-out pattern so that multiple tables can be processed concurrently.
 */
class ChangeLogMonitor(val db: DoorDatabase, private val repo: DoorDatabaseRepository,
    val numProcessors: Int = 5) {

    val tablesChangedChannel: Channel<Int> = Channel(capacity = Channel.UNLIMITED)

    val tablesToProcessChannel: Channel<Int> = Channel(capacity = Channel.UNLIMITED)

    val dispatchJob: AtomicReference<Job?> = AtomicReference(null)

    val changeListenerRequest: DoorDatabase.ChangeListenerRequest

    init {
        changeListenerRequest = DoorDatabase.ChangeListenerRequest(listOf(), this::onTablesChanged)
        db.addChangeListener(changeListenerRequest)

        GlobalScope.launch {
            repeat(numProcessors) {
                launchChangeLogProcessor(it, tablesToProcessChannel)
            }

            //Find anything that was changed when the ChangeLogMonitor wasn't running (e.g. repo not
            // yet created or manually changed by SQL)
            (repo as? DoorDatabaseSyncRepository)?.findTablesWithPendingChangeLogs()?.also {
                onTablesChangedInternal(it)
            }

        }
    }


    fun CoroutineScope.launchChangeLogProcessor(id: Int, channel: Channel<Int>) = launch {
        for(tableId in channel) {
            repo.dispatchUpdateNotifications(tableId)
        }
    }

    fun onTablesChanged(tablesChanged: List<String>) {
        onTablesChangedInternal(tablesChanged.map { repo.tableIdMap[it] ?: 0})
    }

    private fun onTablesChangedInternal(tablesChanged: List<Int>) {
        tablesChanged.forEach {table ->
            tablesChangedChannel.offer(table)

            if(dispatchJob.get() == null) {
                dispatchJob.set(GlobalScope.async {
                    delay(UPDATE_INTERVAL)
                    dispatchJob.set(null)
                    val itemsToSend = mutableSetOf<Int>()
                    var tableId: Int? = null
                    do {
                        tableId = tablesChangedChannel.poll()?.also {
                            itemsToSend += it
                        }
                    }while(tableId != null)

                    itemsToSend.forEach {
                        tablesToProcessChannel.send(it)
                    }
                })
            }
        }
    }



    fun close() {
        db.removeChangeListener(changeListenerRequest)
    }

    companion object {

        const val UPDATE_INTERVAL = 100L

    }

}