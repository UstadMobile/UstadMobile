package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.door.DoorLiveData
import java.lang.IllegalStateException
import java.util.*
import kotlin.coroutines.CoroutineContext
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadRunner
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import com.ustadmobile.core.util.ext.makeRootDownloadJobItem
import com.ustadmobile.core.util.ext.isStatusCompleted
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.door.DoorObserver
import org.kodein.di.*
import com.ustadmobile.sharedse.network.NetworkManagerBle
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.sharedse.io.FileSe

/**
 * This class manages a download queue for a given endpoint.
 */
class ContainerDownloadManagerImpl(private val singleThreadContext: CoroutineContext = newSingleThreadContext("UstadDownloadManager"),
                                   val endpoint: Endpoint, override val di: DI): ContainerDownloadManager(), DIAware {

    var onQueueEmpty: (() -> Unit) = {}

    val appDb: UmAppDatabase by on(endpoint).instance(tag = TAG_DB)

    private val networkManager: NetworkManagerBle by instance()

    private val connectivityObserver = object: DoorObserver<ConnectivityStatus> {
        override fun onChanged(t: ConnectivityStatus) {
            GlobalScope.launch(singleThreadContext) {
                handleConnectivityChanged(t)
            }
        }
    }

    /**
     * This class ensures that a reference is kept as long as anything still holds a reference to
     * this live data. This is used to ensure that holders are not garbage collected as long as
     * anything else holds a reference to the livedata in that holder.
     *
     * IMPORTANT: This requires a proguard rule to keep the reference field. The reference field
     * is otherwise unused. Without a keep rule, shrinking will detect that the reference field
     * is otherwise unused and it will be stripped out. Once it is stripped out, the reference
     * will not prevent garbage collection of the holder when the LiveData is in use as intended.
     *
     */
    inner class MutableLiveDataWithRef<T>: DoorMutableLiveData<T> {

        val reference: Any?

        constructor(reference: Any?): super() {
            this.reference = reference
        }

        constructor(reference: Any?, value: T) : super(value) {
            this.reference = reference
        }

        override fun toString(): String {
            return "MutableLiveDataRef@" + System.identityHashCode(this)
        }

    }

    inner class DownloadJobItemHolder(val downloadJobItemUid: Int,
                                              var downloadJobItem: DownloadJobItem?,
                                              val parents: MutableList<DownloadJobItemHolder>) {

        val liveData = MutableLiveDataWithRef<DownloadJobItem?>(this, downloadJobItem)

        private var downloadJobHolder = loadDownloadJobHolder(downloadJobItem?.djiDjUid ?: -1)

        private val contentEntryHolder = loadContentEntryHolder(downloadJobItem?.djiContentEntryUid ?: -1)

        fun postUpdate(updated: DownloadJobItem, bubble: Boolean = true) {
            val deltaDownloadedSoFar = updated.downloadedSoFar - (downloadJobItem?.downloadedSoFar ?: 0L)
            val deltaDownloadLength = updated.downloadLength - (downloadJobItem?.downloadLength ?: 0L)
            val statusChanged = updated.djiStatus != downloadJobItem?.djiStatus
            downloadJobItem = updated

            if(updated.djiDjUid != downloadJobHolder.downloadJobUid)
                downloadJobHolder = loadDownloadJobHolder(updated.djiDjUid)

            postUpdateToDownloadJobIfRootEntry(deltaDownloadedSoFar, deltaDownloadLength,
                    updated.djiStatus)

            liveData.sendValue(updated)

            contentEntryHolder.liveData.sendValue(updated)

            entriesToCommit.add(updated)

            parents.takeIf{ bubble }?.forEach {
                it.postUpdateInternal(deltaDownloadedSoFar, deltaDownloadLength, statusChanged)
            }
        }

        fun postUpdateInternal(deltaDownloadedSoFar: Long, deltaDownloadLength: Long,
                               statusChanged: Boolean) {
            val jobItemVal = downloadJobItem
            var thisStatusChanged = false
            if(jobItemVal != null) {
                jobItemVal.downloadedSoFar += deltaDownloadedSoFar
                jobItemVal.downloadLength += deltaDownloadLength

                if(statusChanged) {
                    val childStatuses = appDb.downloadJobItemDao.getUidAndStatusByParentJobItem(jobItemVal.djiUid)
                    childStatuses.forEach {
                        val loadedStatus = jobItemUidToHolderMap[it.djiUid]?.get()?.downloadJobItem?.djiStatus
                        if(loadedStatus != null)
                            it.djiStatus = loadedStatus
                    }

                    val newStatus = determineParentStatusFromChildStatuses(childStatuses)
                    if(newStatus != jobItemVal.djiStatus) {
                        jobItemVal.djiStatus = newStatus
                        thisStatusChanged = true
                    }
                }

                entriesToCommit.add(jobItemVal)
                liveData.sendValue(jobItemVal)
                val contentEntryHolder = contentEntryHolders[jobItemVal.djiContentEntryUid]?.get()
                contentEntryHolder?.liveData?.sendValue(jobItemVal)
                postUpdateToDownloadJobIfRootEntry(deltaDownloadedSoFar, deltaDownloadLength,
                        jobItemVal.djiStatus)
            }else {
                throw IllegalStateException("Can't update or increment a null item")
                //something very wrong
            }

            parents.takeIf { thisStatusChanged || deltaDownloadedSoFar > 0 || deltaDownloadLength> 0 }
                    ?.forEach {
                it.postUpdateInternal(deltaDownloadedSoFar, deltaDownloadLength, thisStatusChanged)
            }
        }

        /**
         * If this is the root DownloadJobItem for a DownloadJob, then apply changes to the DownloadJob itself
         */
        private fun postUpdateToDownloadJobIfRootEntry(deltaDownloadedSoFar: Long,
                                                       deltaDownloadLength: Long, newStatus: Int) {
            if(parents.isEmpty()
                    && downloadJobItem?.djiContentEntryUid == downloadJobHolder.downloadJob?.djRootContentEntryUid) {
                downloadJobHolder.postUpdateInternal(deltaDownloadedSoFar, deltaDownloadLength, newStatus)
            }
        }
    }

    inner class ContentEntryHolder(val contentEntryUid: Long, var downloadJobItem: DownloadJobItem?) {

        val liveData = MutableLiveDataWithRef<DownloadJobItem?>(this, downloadJobItem)

        fun postUpdate(update: DownloadJobItem?) {
            downloadJobItem = update
            liveData.sendValue(update)
        }

    }

    private inner class DownloadJobHolder(val downloadJobUid: Int, var downloadJob: DownloadJob?) {

        val liveData = MutableLiveDataWithRef<DownloadJob?>(this, downloadJob)

        fun postUpdate(update: DownloadJob) {
            downloadJob = update
            liveData.sendValue(update)
            jobsToCommit.add(update)
        }

        fun postUpdateInternal(deltaDownloadedSoFar: Long, deltaDownloadLength: Long,
                               newStatus: Int) {
            val downloadJobVal = downloadJob
            if(downloadJobVal != null) {
                downloadJobVal.bytesDownloadedSoFar += deltaDownloadedSoFar
                downloadJobVal.totalBytesToDownload += deltaDownloadLength
                downloadJobVal.djStatus = newStatus
                liveData.sendValue(downloadJobVal)
                jobsToCommit.add(downloadJobVal)
            }
        }

    }

    private data class ActiveContainerDownload(val downloadJobItem: DownloadJobItem,
                                               val runner: ContainerDownloadRunner)


    private val jobItemUidToHolderMap = HashMap<Int, WeakReference<DownloadJobItemHolder>>()

    private val contentEntryHolders = HashMap<Long, WeakReference<ContentEntryHolder>>()

    private val downloadJobMap = HashMap<Int, WeakReference<DownloadJobHolder>>()

    private val entriesToCommit : MutableSet<DownloadJobItem> = HashSet()

    private val jobsToCommit: MutableSet<DownloadJob> = HashSet()

    private var currentConnectivityStatus: ConnectivityStatus? = null

    private val activeDownloads: MutableMap<Int, ActiveContainerDownload> = ConcurrentHashMap()

    val maxNumConcurrentDownloads = 1

    init {
        GlobalScope.launch(doorMainDispatcher()) {
            networkManager.connectivityStatus.observeForever(connectivityObserver)
        }
    }


    override suspend fun getDownloadJobItemByJobItemUid(jobItemUid: Int): DoorLiveData<DownloadJobItem?> = withContext(singleThreadContext){
        val holder = loadDownloadJobItemHolder(jobItemUid)

        holder.liveData
    }

    override suspend fun getDownloadJobRootItem(jobUid: Int): DownloadJobItem? = withContext(singleThreadContext){
        val downloadJobItemHolder = loadDownloadJobItemHolder(0) {
            appDb.downloadJobItemDao.findRootForDownloadJob(jobUid)
        }

        downloadJobItemHolder.downloadJobItem
    }

    private fun loadDownloadJobItemHolder(jobItemUid: Int,
                                          loadFn: () -> DownloadJobItem? = {appDb.downloadJobItemDao.findByUid(jobItemUid)}) : DownloadJobItemHolder{
        val currentHolder = jobItemUidToHolderMap[jobItemUid]?.get()
        if(currentHolder != null)
            return currentHolder

        val downloadJobItem = loadFn()
        val parents = appDb.downloadJobItemParentChildJoinDao.findParentsByChildUid(jobItemUid)
        val parentHolders = parents.map { loadDownloadJobItemHolder(it.djiParentDjiUid) }
        val newHolder = DownloadJobItemHolder(jobItemUid, downloadJobItem, parentHolders.toMutableList())
        jobItemUidToHolderMap[jobItemUid] = WeakReference(newHolder)
        return newHolder
    }

    suspend fun getDownloadJobItemHolder(jobItemUid: Int) = withContext(singleThreadContext) {
        loadDownloadJobItemHolder(jobItemUid)
    }

    private fun loadDownloadJobHolder(jobUid: Int): DownloadJobHolder {
        val currentHolder = downloadJobMap[jobUid]?.get()
        if(currentHolder != null)
            return currentHolder

        val downloadJob = appDb.downloadJobDao.findByUid(jobUid)
        val newHolder = DownloadJobHolder(jobUid, downloadJob)
        downloadJobMap[jobUid] = WeakReference(newHolder)
        return newHolder
    }

    override suspend fun commit() = withContext(singleThreadContext){
        appDb.downloadJobItemDao.updateStatusAndProgressList(entriesToCommit.toList())
        appDb.downloadJobDao.updateStatusAndProgressList(jobsToCommit.toList())
        entriesToCommit.clear()
        jobsToCommit.clear()
    }


    override suspend fun getDownloadJobItemByContentEntryUid(contentEntryUid: Long): DoorLiveData<DownloadJobItem?> = withContext(singleThreadContext){
        loadContentEntryHolder(contentEntryUid).liveData
    }

    private fun loadContentEntryHolder(contentEntryUid: Long, initDownloadJob: DownloadJobItem? = null): ContentEntryHolder {
        var currentHolder = contentEntryHolders[contentEntryUid]?.get()
        if(currentHolder != null)
            return currentHolder

        var currentDownloadJob = initDownloadJob ?: jobItemUidToHolderMap.values.firstOrNull {
            it.get()?.downloadJobItem?.djiContentEntryUid == contentEntryUid
        }?.get()?.downloadJobItem

        if(currentDownloadJob == null) {
            currentDownloadJob = appDb.downloadJobItemDao.findByContentEntryUid(contentEntryUid)
        }

        currentHolder = ContentEntryHolder(contentEntryUid, currentDownloadJob)
        contentEntryHolders[contentEntryUid] = WeakReference(currentHolder)
        return currentHolder
    }

    suspend fun getContentEntryHolder(contentEntryUid: Long) = withContext(singleThreadContext){
        loadContentEntryHolder(contentEntryUid)
    }

    override suspend fun getDownloadJob(jobUid: Int): DoorLiveData<DownloadJob?> = withContext(singleThreadContext) {
        loadDownloadJobHolder(jobUid).liveData
    }

    override suspend fun createDownloadJob(downloadJob: DownloadJob) = withContext(singleThreadContext){
        downloadJob.djUid = appDb.downloadJobDao.insert(downloadJob).toInt()
        downloadJobMap[downloadJob.djUid] = WeakReference(DownloadJobHolder(downloadJob.djUid, downloadJob))

        val rootEntryContainer = appDb.containerDao
                .getMostRecentContainerForContentEntry(downloadJob.djRootContentEntryUid)
        val rootDownloadJobItem = downloadJob.makeRootDownloadJobItem(rootEntryContainer)
        addItemsToDownloadJob(listOf(rootDownloadJobItem))
    }

    override suspend fun addItemsToDownloadJob(newItems: List<DownloadJobItemWithParents>) = withContext(singleThreadContext){
        newItems.forEach {downloadJobItem ->
            downloadJobItem.djiUid = appDb.downloadJobItemDao.insert(downloadJobItem).toInt()
            downloadJobItem.parents.forEach {
                it.djiChildDjiUid = downloadJobItem.djiUid
                it.djiPcjUid = appDb.downloadJobItemParentChildJoinDao.insert(it).toInt()
            }

            val emptyDownloadJobItem = DownloadJobItem(downloadJobItem).also {
                it.downloadLength = 0L
                it.downloadedSoFar = 0L
            }

            val holder = DownloadJobItemHolder(downloadJobItem.djiUid, emptyDownloadJobItem,
                    downloadJobItem.parents.map { loadDownloadJobItemHolder(it.djiParentDjiUid) }.toMutableList())
            jobItemUidToHolderMap[downloadJobItem.djiUid] = WeakReference(holder)

            //now post an update so that counts etc. are incremented
            holder.postUpdate(downloadJobItem)
        }

        commit()
    }

    /**
     * Updates the status of all items that are waiting (e.g. queued, waiting for connectivity, etc)
     * or those that are actively running
     */
    private fun updateWaitingAndActiveStatuses(downloadJobId: Int, pendingJobStatus: Int,
                                               actionOnActiveRunners: ContainerDownloadRunner.() -> Unit) {

        //On active jobs we invoke a method on the ContainerDownloadRunner itself. It is then up
        // to the runner to take action and then update it's status accordingly.
        val activeJobUids = mutableListOf<Int>()
        activeDownloads.values.filter { it.downloadJobItem.djiDjUid == downloadJobId }
                .forEach {
                    activeJobUids.add(it.downloadJobItem.djiUid)
                    actionOnActiveRunners.invoke(it.runner)
                }

        val jobItemHoldersUpdated = mutableSetOf<Int>()
        jobItemUidToHolderMap.values.map { it.get() }.filter { it != null }
                .map { it!! }
                .filter { it.downloadJobItem?.djiDjUid == downloadJobId
                        && (it.downloadJobItem?.djiUid ?: -1) !in activeJobUids
                        && (it.downloadJobItem?.djiStatus ?: 0) < JobStatus.RUNNING_MIN }.forEach {
                    val currentDji = it.downloadJobItem
                    if(currentDji != null) {
                        currentDji.djiStatus = pendingJobStatus
                        jobItemHoldersUpdated.add(currentDji.djiUid)
                        it.postUpdate(currentDji)
                    }
                }

        this.contentEntryHolders.values.map { it.get() }.filter { it != null }
                .map { it!! }
                .filter { (it.downloadJobItem?.djiUid ?: 0) !in jobItemHoldersUpdated
                        && it.downloadJobItem?.djiDjUid == downloadJobId }.forEach {
                    val downloadJobItemVal = it.downloadJobItem
                    if(downloadJobItemVal != null) {
                        downloadJobItemVal.djiStatus = pendingJobStatus
                        it.postUpdate(downloadJobItemVal)
                    }
                }



        //Update the status of any waiting items that are not loaded in memory at the moment
        // any item that had a status of running would be in memory.
        appDb.downloadJobItemDao.updateWaitingItemStatus(downloadJobId, pendingJobStatus,
                activeJobUids)

    }

    private suspend fun checkQueue() {
        if(activeDownloads.size >= maxNumConcurrentDownloads) {
            return
        }

        val nextDownload = appDb.downloadJobItemDao.findNextDownloadJobItems2(1,
                currentConnectivityStatus?.connectivityState ?: ConnectivityStatus.STATE_DISCONNECTED)
        if(nextDownload.isNotEmpty()) {
            val containerDownloader: ContainerDownloadRunner = di.direct.instance(
                    arg = DownloadJobItemRunnerDIArgs(endpoint, nextDownload[0]))

            activeDownloads[nextDownload[0].djiUid] = ActiveContainerDownload(nextDownload[0],
                    containerDownloader)
            GlobalScope.launch(Dispatchers.IO) {
                containerDownloader.download()
            }
        }else {
            onQueueEmpty()
            if(currentConnectivityStatus?.connectivityState == ConnectivityStatus.STATE_CONNECTED_LOCAL) {
                networkManager.restoreWifi()
            }
        }
    }

    override suspend fun handleDownloadJobItemUpdated(downloadJobItem: DownloadJobItem, autoCommit: Boolean) = withContext(singleThreadContext){
        val holder = loadDownloadJobItemHolder(downloadJobItem.djiUid)
        holder.postUpdate(downloadJobItem)
        if(autoCommit)
            commit()

        if(downloadJobItem != null
                && (downloadJobItem.djiStatus >= JobStatus.COMPLETE_MIN
                        || downloadJobItem.djiStatus < JobStatus.RUNNING_MIN)) {
            val downloadRemoved = activeDownloads.remove(downloadJobItem.djiUid)
            if(downloadRemoved != null) {
                checkQueue()
            }
        }
    }

    override suspend fun deleteDownloadJob(jobUid: Int, onprogress: (progress: Int) -> Unit): Boolean {
        appDb.downloadJobDao.findByUid(jobUid)?: return false

        appDb.downloadJobItemDao.forAllChildDownloadJobItemsRecursiveAsync(jobUid) { childItems ->
            childItems.forEach {
                appDb.containerEntryDao.deleteByContentEntryUid(it.djiContentEntryUid)

                handleDownloadJobItemUpdated(DownloadJobItem(it).also {
                    it.djiStatus = JobStatus.DELETED
                }, autoCommit = false)
            }
        }

        commit()

        var numFailures = 0
        appDb.runInTransaction(Runnable {
            val zombieEntryFilesList = appDb.containerEntryFileDao.findZombieEntries()
            zombieEntryFilesList.forEach {
                val filePath = it.cefPath
                if(filePath == null || !FileSe(filePath).delete()) {
                    numFailures++
                }
            }
            onprogress.invoke(100)

            appDb.containerEntryFileDao.deleteListOfEntryFiles(zombieEntryFilesList)
        })

        return numFailures == 0
    }

    override suspend fun handleDownloadJobUpdated(downloadJob: DownloadJob) = withContext(singleThreadContext){
        val holder = loadDownloadJobHolder(downloadJob.djUid)
        holder.postUpdate(downloadJob)
    }

    override suspend fun enqueue(downloadJobId: Int) = withContext(singleThreadContext){
        updateWaitingAndActiveStatuses(downloadJobId, JobStatus.QUEUED, {})
        commit()
        checkQueue()
    }

    override suspend fun pause(downloadJobId: Int) = withContext(singleThreadContext){
        updateWaitingAndActiveStatuses(downloadJobId, JobStatus.PAUSED, {
            GlobalScope.launch { pause() }
        })
        commit()
    }

    override suspend fun cancel(downloadJobId: Int) = withContext(singleThreadContext) {
        updateWaitingAndActiveStatuses(downloadJobId, JobStatus.CANCELED, {
            val downloadRunner = this
            println("Download runner = $downloadRunner")
            GlobalScope.launch { downloadRunner.cancel() }
        })
        commit()
    }

    override suspend fun setMeteredDataAllowed(downloadJobUid: Int, meteredDataAllowed: Boolean) = withContext(singleThreadContext) {
        appDb.downloadJobDao.setMeteredConnectionAllowedByJobUidSync(downloadJobUid, meteredDataAllowed)
        val downloadJobHolder = downloadJobMap[downloadJobUid]?.get()
        val downloadJob = downloadJobHolder?.downloadJob
        if(downloadJobHolder != null && downloadJob != null) {
            downloadJob.meteredNetworkAllowed = meteredDataAllowed
            downloadJobHolder.postUpdate(downloadJob)
        }

        activeDownloads.values.filter { it.downloadJobItem.djiDjUid == downloadJobUid }
                .forEach {
                    it.runner.meteredDataAllowed = meteredDataAllowed
                }


        if(meteredDataAllowed)
            checkQueue()
    }

    override suspend fun handleConnectivityChanged(status: ConnectivityStatus) = withContext(singleThreadContext){
        currentConnectivityStatus = status
        checkQueue()
    }
}