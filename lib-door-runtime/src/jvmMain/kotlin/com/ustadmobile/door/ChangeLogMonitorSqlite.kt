package com.ustadmobile.door

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.sqlite.SQLiteConnection
import org.sqlite.SQLiteUpdateListener
import java.util.concurrent.atomic.AtomicReference

class ChangeLogMonitorSqlite(db: DoorDatabase, private val repo: DoorDatabaseRepository): SQLiteUpdateListener {

    val connection: SQLiteConnection

    val channel: Channel<Int> = Channel(capacity = Channel.UNLIMITED)

    val dispatchJob: AtomicReference<Job?> = AtomicReference(null)

    init {
        connection = db.openConnection() as SQLiteConnection
        connection.addUpdateListener(this)
    }

    override fun onUpdate(type: SQLiteUpdateListener.Type?, database: String?, table: String?, rowId: Long) {
        if(table != null) {
            channel.offer(repo.tableIdMap[table] ?: 0)
            if(dispatchJob.get() == null) {
                dispatchJob.set(GlobalScope.async {
                    delay(100)
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
        connection.removeUpdateListener(this)
        connection.close()
    }

    companion object {

        const val UPDATE_INTERVAL = 100

    }

}