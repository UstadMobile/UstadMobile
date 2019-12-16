package com.ustadmobile.sharedse.network

import androidx.lifecycle.MutableLiveData
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorLiveData
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException
import java.util.*
import kotlin.coroutines.CoroutineContext
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadRunner
import com.ustadmobile.lib.db.entities.*
import java.lang.ref.WeakReference
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class DownloadManagerImpl(private val singleThreadContext: CoroutineContext = newSingleThreadContext("UstadDownloadManager"),
                          private val appDb: UmAppDatabase,
                          private val downloadJobRunner: suspend (DownloadJobItem) -> ContainerDownloadRunner): ContainerDownloadManager() {

    /**
     * This class ensures that a reference is kept as long as anything still holds a reference to
     * this live data. This is used to ensure that holders are not garbage collected as long as
     * anything else holds a reference to the livedata in that holder
     */
    private inner class MutableLiveDataWithRef<T>: MutableLiveData<T> {

        val reference: Any?

        constructor(reference: Any?): super() {
            this.reference = reference
        }

        constructor(reference: Any?, value: T) : super(value) {
            this.reference = reference
        }

    }

    private inner class DownloadJobItemHolder(val downloadJobItemUid: Int,
                                              var downloadJobItem: DownloadJobItem?,
                                              val parents: MutableList<DownloadJobItemHolder>) {

        val liveData = MutableLiveDataWithRef<DownloadJobItem?>(this, downloadJobItem)

        fun postUpdate(updated: DownloadJobItem, bubble: Boolean = true) {
            val deltaDownloadedSoFar = updated.downloadedSoFar - (downloadJobItem?.downloadedSoFar ?: 0L)
            val deltaDownloadLength = updated.downloadLength - (downloadJobItem?.downloadLength ?: 0L)
            val statusChanged = updated.djiStatus != downloadJobItem?.djiStatus
            downloadJobItem = updated

            liveData.postValue(updated)
            contentEntryHolders[updated.djiContentEntryUid]?.get()?.liveData?.postValue(updated)

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
                liveData.postValue(jobItemVal)
                val contentEntryHolder = contentEntryHolders[jobItemVal.djiContentEntryUid]?.get()
                contentEntryHolder?.liveData?.postValue(jobItemVal)
            }else {
                throw IllegalStateException("Can't update or increment a null item")
                //something very wrong
            }

            parents.takeIf { thisStatusChanged || deltaDownloadedSoFar > 0 || deltaDownloadLength> 0 }
                    ?.forEach {
                it.postUpdateInternal(deltaDownloadedSoFar, deltaDownloadLength, thisStatusChanged)
            }
        }
    }

    private inner class ContentEntryHolder(val contentEntryUid: Long, downloadJobItem: DownloadJobItem?) {

        val liveData = MutableLiveDataWithRef<DownloadJobItem?>(this, downloadJobItem)

    }

    private val jobItemUidToHolderMap = HashMap<Int, WeakReference<DownloadJobItemHolder>>()

    private val contentEntryHolders = HashMap<Long, WeakReference<ContentEntryHolder>>()

    private val downloadJobMap = WeakHashMap<Int, DownloadJob>()

    private val entriesToCommit : MutableSet<DownloadJobItem> = HashSet()

    override suspend fun getDownloadJobItemByJobItemUid(jobItemUid: Int): DoorLiveData<DownloadJobItem?> = withContext(singleThreadContext){
        val holder = loadDownloadJobItemHolder(jobItemUid)

        holder.liveData
    }

    private fun loadDownloadJobItemHolder(jobItemUid: Int) : DownloadJobItemHolder{
        val currentHolder = jobItemUidToHolderMap[jobItemUid]?.get()
        if(currentHolder != null)
            return currentHolder

        val downloadJob = appDb.downloadJobItemDao.findByUid(jobItemUid)
        val parents = appDb.downloadJobItemParentChildJoinDao.findParentsByChildUid(jobItemUid)
        val parentHolders = parents.map { loadDownloadJobItemHolder(it.djiParentDjiUid) }
        val newHolder = DownloadJobItemHolder(jobItemUid, downloadJob, parentHolders.toMutableList())
        jobItemUidToHolderMap[jobItemUid] = WeakReference(newHolder)
        return newHolder
    }

    suspend fun commit() = withContext(singleThreadContext){
        appDb.downloadJobItemDao.updateStatusAndProgressList(entriesToCommit.toList())
        entriesToCommit.clear()
    }

    override suspend fun getDownloadJobItemByContentEntryUid(contentEntryUid: Long): DoorLiveData<DownloadJobItem?> {
        var currentHolder = contentEntryHolders[contentEntryUid]?.get()
        if(currentHolder == null) {
            var currentDownloadJob = jobItemUidToHolderMap.values.firstOrNull {
                it.get()?.downloadJobItem?.djiContentEntryUid == contentEntryUid
            }?.get()?.downloadJobItem

            if(currentDownloadJob == null) {
                currentDownloadJob = appDb.downloadJobItemDao.findByContentEntryUid(contentEntryUid)
            }

            currentHolder = ContentEntryHolder(contentEntryUid, currentDownloadJob)
            contentEntryHolders[contentEntryUid] = WeakReference(currentHolder)
        }

        return currentHolder.liveData
    }

    override suspend fun createDownloadJob(downloadJob: DownloadJob) = withContext(singleThreadContext){
        downloadJob.djUid = appDb.downloadJobDao.insert(downloadJob).toInt()
        downloadJobMap[downloadJob.djUid] = downloadJob
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

    private fun updateDownloadJobStatusInternal(activeJobStatus: Int, completeJobStatus: Int) {
        //first find the active jobs that are actually running - stop them
    }

    override suspend fun handleDownloadJobItemUpdated(downloadJobItem: DownloadJobItem) = withContext(singleThreadContext){
        loadDownloadJobItemHolder(downloadJobItem.djiUid).postUpdate(downloadJobItem)
    }

    override suspend fun enqueue(downloadJobId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun pause(downloadJobId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun cancel(downloadJobId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun setMeteredDataAllowed(downloadJobUid: Int, meteredDataAllowed: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun handleConnectivityChanged(status: ConnectivityStatus) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}