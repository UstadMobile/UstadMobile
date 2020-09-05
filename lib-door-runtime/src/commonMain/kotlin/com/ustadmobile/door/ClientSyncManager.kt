package com.ustadmobile.door

import com.github.aakira.napier.Napier
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

class ClientSyncManager(val repo: DoorDatabaseSyncRepository, val maxProcessors: Int = 5, initialConnectivitySfieldtatus: Int) {

    val updateCheckJob: AtomicRef<Job?> = atomic(null)

    //TOOD: replace this with copyonWriteListOf
    val activeJobs: MutableList<Int> = mutableListOf()

    val channel = Channel<Int>(Channel.UNLIMITED)

    var connectivityStatus: Int = initialConnectivitySfieldtatus
        set(value) {
            field = value
            if(value == DoorDatabaseRepository.STATUS_CONNECTED)
                checkQueue()
        }

    fun CoroutineScope.launchProcessor(id: Int, channel: Channel<Int>) = launch {
        for(tableId in channel) {
            ///TODO: specify the table id
            activeJobs += tableId
            try {
                repo.sync(null)
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
    }

    fun invalidate() {
        if(updateCheckJob.value == null) {
            updateCheckJob.value = GlobalScope.async {
                delay(1000)
                updateCheckJob.value = null
                checkQueue()
            }
        }
    }

    fun checkQueue() {
        //TODO: see which tables need synced (if any), then send them on the channel

    }

}