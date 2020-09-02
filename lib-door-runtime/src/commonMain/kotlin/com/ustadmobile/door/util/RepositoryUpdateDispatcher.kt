package com.ustadmobile.door.util

import com.ustadmobile.door.DoorDatabaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class RepositoryUpdateDispatcher(val repo: DoorDatabaseRepository, val numProcessors: Int = 5) : RepositoryPendingChangeLogListener{

    val channel = Channel<Int>(capacity = Channel.UNLIMITED)

    fun CoroutineScope.launchProcessor(id: Int, channel: Channel<Int>) = launch {
        for(tableId in channel) {
            repo.dispatchUpdateNotifications(tableId)
        }
    }

    init {
        GlobalScope.launch {
            repeat(numProcessors) {
                launchProcessor(it, channel)
            }
        }
    }

    override fun onPendingChangeLog(tableIdList: Set<Int>) {
        tableIdList.forEach {
            channel.offer(it)
        }
    }

}