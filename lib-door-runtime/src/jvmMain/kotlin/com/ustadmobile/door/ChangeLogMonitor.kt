package com.ustadmobile.door

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.atomic.AtomicReference

class ChangeLogMonitor(val db: DoorDatabase, private val repo: DoorDatabaseRepository) {

    val channel: Channel<Int> = Channel(capacity = Channel.UNLIMITED)

    val dispatchJob: AtomicReference<Job?> = AtomicReference(null)

    val changeListenerRequest: DoorDatabase.ChangeListenerRequest

    init {
        changeListenerRequest = DoorDatabase.ChangeListenerRequest(listOf(), this::onTablesChanged)
        db.addChangeListener(changeListenerRequest)
    }


    fun onTablesChanged(tablesChanged: List<String>) {
        tablesChanged.forEach {table ->
            channel.offer(repo.tableIdMap[table] ?: 0)

            if(dispatchJob.get() == null) {
                dispatchJob.set(GlobalScope.async {
                    delay(100)
                    dispatchJob.set(null)
                    val itemsToSend = mutableSetOf<Int>()
                    var tableId: Int? = null
                    do {
                        tableId = channel.poll()?.also {
                            itemsToSend += it
                        }
                    }while(tableId != null)
                    repo.onPendingChangeLog(itemsToSend)
                })
            }
        }
    }

    fun close() {
        db.removeChangeListener(changeListenerRequest)
    }

    companion object {

        const val UPDATE_INTERVAL = 100

    }

}