package com.ustadmobile.sharedse.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.db.dao.DownloadJobDao
import com.ustadmobile.door.DoorLiveData
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException
import java.util.*
import kotlin.coroutines.CoroutineContext
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadRunner
import com.ustadmobile.lib.db.entities.*

class DownloadManagerImpl(private val singleThreadContext: CoroutineContext = newSingleThreadContext("UstadDownloadManager"),
                          private val appDb: UmAppDatabase,
                          private val downloadJobRunner: suspend (DownloadJobItem) -> ContainerDownloadRunner): ContainerDownloadManager {

    private inner class DownloadJobItemHolder(val downloadJobItemUid: Int,
                                              var downloadJobItem: DownloadJobItem?,
                                              val parents: MutableList<DownloadJobItemHolder>) {

        val liveData = MutableLiveData<DownloadJobItem?>(downloadJobItem)

        fun postUpdate(updated: DownloadJobItem) {
            val deltaDownloadedSoFar = updated.downloadedSoFar - (downloadJobItem?.downloadedSoFar ?: 0L)
            val deltaDownloadLength = updated.downloadLength - (downloadJobItem?.downloadLength ?: 0L)
            val statusChanged = updated.djiStatus != downloadJobItem?.djiStatus
            downloadJobItem = updated

            liveData.postValue(updated)

            parents.forEach {
                it.postUpdateInternal(deltaDownloadedSoFar, deltaDownloadLength, statusChanged)
            }
        }

        fun postUpdateInternal(deltaDownloadedSoFar: Long, deltaDownloadLength: Long,
                               statusChanged: Boolean) {
            val jobItemVal = downloadJobItem
            if(jobItemVal != null) {
                jobItemVal.downloadedSoFar += deltaDownloadedSoFar
                jobItemVal.downloadLength += deltaDownloadLength

                if(statusChanged) {
                    //TODO: Set the new status - run a query to find all children
                }

                liveData.postValue(jobItemVal)
            }else {
                throw IllegalStateException("Can't update or increment a null item")
                //something very wrong
            }

            parents.forEach {
                it.postUpdateInternal(deltaDownloadedSoFar, deltaDownloadLength, statusChanged)
            }
        }


    }

    private val jobItemUidToHolderMap = WeakHashMap<Int, DownloadJobItemHolder>()

    private val contentEntryUidToLiveDataMap = WeakHashMap<Long, MutableLiveData<DownloadJobItem>>()

    private val downloadJobMap = WeakHashMap<Int, DownloadJob>()

    override suspend fun getDownloadJobItemByJobItemUid(jobItemUid: Int): DoorLiveData<DownloadJobItem?> = withContext(singleThreadContext){
        val holder = loadDownloadJobItemHolder(jobItemUid)

        holder.liveData
    }

    private fun loadDownloadJobItemHolder(jobItemUid: Int) : DownloadJobItemHolder{
        val currentHolder = jobItemUidToHolderMap[jobItemUid]
        if(currentHolder != null)
            return currentHolder

        val downloadJob = appDb.downloadJobItemDao.findByUid(jobItemUid)
        val parents = appDb.downloadJobItemParentChildJoinDao.findParentsByChildUid(jobItemUid)
        val parentHolders = parents.map { loadDownloadJobItemHolder(it.djiParentDjiUid) }
        val newHolder = DownloadJobItemHolder(jobItemUid, downloadJob, parentHolders.toMutableList())
        jobItemUidToHolderMap[jobItemUid] = newHolder
        return newHolder
    }


    override suspend fun getDownloadJobItemByContentEntryUid(contentEntryUid: Int): DoorLiveData<DownloadJobItem?> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
            jobItemUidToHolderMap[downloadJobItem.djiUid] = holder

            //now post an update so that counts etc. are incremented
            holder.postUpdate(downloadJobItem)
        }
    }

    override suspend fun handleDownloadJobItemUpdated(downloadJobItem: DownloadJobItem) = withContext(singleThreadContext){

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