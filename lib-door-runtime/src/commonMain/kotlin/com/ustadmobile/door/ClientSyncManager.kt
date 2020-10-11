package com.ustadmobile.door

import com.github.aakira.napier.Napier
import com.ustadmobile.door.DoorConstants.HEADER_DBVERSION
import com.ustadmobile.door.DoorDatabaseRepository.Companion.STATUS_CONNECTED
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.DoorTag.Companion.LOG_TAG
import com.ustadmobile.door.ext.doorIdentityHashCode
import com.ustadmobile.door.sse.DoorEventListener
import com.ustadmobile.door.sse.DoorEventSource
import com.ustadmobile.door.sse.DoorServerSentEvent
import com.ustadmobile.door.util.systemTimeInMillis
import io.ktor.client.HttpClient
import io.ktor.client.request.get
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
                        initialConnectivityStatus: Int,
                        private val syncDaoSubscribePath: String,
                        private val syncDaoNotificationReceivedPath: String,
                        val httpClient: HttpClient,
                        val maxProcessors: Int = 5): TableChangeListener {

    val updateCheckJob: AtomicRef<Job?> = atomic(null)

    //TOOD: replace this with copyonWriteListOf
    val pendingJobs: MutableList<Int> = mutableListOf()

    val channel = Channel<Int>(Channel.UNLIMITED)

    val eventSource: AtomicRef<DoorEventSource?> = atomic(null)

    val eventSourceLock = Mutex()

    val logPrefix: String by lazy(LazyThreadSafetyMode.NONE) {
        "[ClientSyncManager@${this.doorIdentityHashCode}]"
    }

    var connectivityStatus: Int = initialConnectivityStatus
        set(value) {
            field = value
            GlobalScope.launch {
                if(value == STATUS_CONNECTED) {
                    Napier.d("$logPrefix connected - checking EventSource", tag = LOG_TAG)
                    checkEndpointEventSource()
                }else {
                    Napier.d("$logPrefix disconnected - closing EventSource", tag = LOG_TAG)
                    eventSourceLock.withLock {
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
                Napier.d("$logPrefix start repo sync for #$tableId", tag = LOG_TAG)
                val syncResults = repo.sync(listOf(tableId))
                Napier.d("$logPrefix finish repo sync for $tableId " +
                        "received ${syncResults.firstOrNull()?.received} / " +
                        "sent ${syncResults.firstOrNull()?.sent}", tag = LOG_TAG)
                repo.takeIf { syncResults.any { it.tableId == tableId && it.status == SyncResult.STATUS_SUCCESS} }
                        ?.updateTableSyncStatusLastSynced(tableId, startTime)
                pendingJobs -= tableId
                checkQueue()
            }catch(e: Exception) {
                Napier.e("$logPrefix Exception syncing tableid #$id", e, tag = LOG_TAG)
            }
        }
    }

    init {
        GlobalScope.launch {
            Napier.d("$logPrefix init", tag = LOG_TAG)
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

            val url = "${repo.endpoint}$syncDaoSubscribePath?deviceId=${repo.clientId}&$HEADER_DBVERSION=$dbVersion"
            Napier.v("$logPrefix subscribing to updates from $url", tag = LOG_TAG)
            eventSource.value = DoorEventSource(url,
                    object : DoorEventListener {
                override fun onOpen() {

                }

                override fun onMessage(message: DoorServerSentEvent) {
                    Napier.v("$logPrefix : Message: ${message.event} - ${message.data}",
                        tag = LOG_TAG)
                    if(message.event.equals("update", true)) {
                        val lineParts = message.data.split(REGEX_WHITESPACE)
                        GlobalScope.launch {
                            val tableId = lineParts[0].toInt()
                            val lastModified = lineParts[1].toLong()
                            Napier.v("$logPrefix - update last changed for $tableId to $lastModified",
                                    tag = LOG_TAG)
                            repo.updateTableSyncStatusLastChanged(tableId, systemTimeInMillis())
                            invalidate()
                            httpClient.get<Unit>("${repo.endpoint}$syncDaoNotificationReceivedPath?" +
                                    "deviceId=${repo.clientId}&$HEADER_DBVERSION=$dbVersion&tableId=$tableId&lastModTimestamp=$lastModified")
                        }
                    }
                }

                override fun onError(e: Exception) {
                    Napier.e("$logPrefix EventSource onError", throwable = e, tag = LOG_TAG)
                }
            })
        }
    }


    override fun onTableChanged(tableName: String) {
        val tableId = repo.tableIdMap[tableName] ?: -1
        GlobalScope.launch {
            Napier.d("$logPrefix tableChanged: $tableName #$tableId", tag = LOG_TAG)
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
        Napier.v("$logPrefix checkQueue found ${newJobs.size} tables to sync", tag = LOG_TAG)
        newJobs.subList(0, min(maxProcessors, newJobs.size)).forEach {
            Napier.d("$logPrefix send table id #${it.tsTableId} to sync fan-out", tag = LOG_TAG)
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