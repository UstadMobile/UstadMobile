package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.DownloadJobItemStatusProvider
import com.ustadmobile.core.networkmanager.OnDownloadJobItemChangeListener
import com.ustadmobile.lib.db.entities.DownloadJob
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus
import com.ustadmobile.lib.util.copyOnWriteListOf
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Manages a list of DownloadJobItemManagers. Creates new items, closes downloadjobs that have finished etc.
 */
class DownloadJobItemManagerList(private val appDatabase: UmAppDatabase,
                                 private val coroutineDispatchCreator: (String) -> CoroutineDispatcher) : DownloadJobItemStatusProvider {


    private val managerMap = mutableMapOf<Int, DownloadJobItemManager>()

    private val changeListeners = copyOnWriteListOf<OnDownloadJobItemChangeListener>()

    fun createNewDownloadJobItemManager(newDownloadJob: DownloadJob): DownloadJobItemManager {
        newDownloadJob.djUid = appDatabase.downloadJobDao.insert(newDownloadJob).toInt()
        //TODO: fix this
        val manager = DownloadJobItemManager(appDatabase, newDownloadJob.djUid,
                coroutineDispatchCreator("DownloadJob-${newDownloadJob.djUid}"))
        manager.onDownloadJobItemChangeListener = this::onDownloadJobItemChange
        managerMap[newDownloadJob.djUid] = manager

        return manager
    }

    fun getDownloadJobItemManager(downloadJobId: Int): DownloadJobItemManager? {
        return managerMap.get(downloadJobId)
    }

    fun getActiveDownloadJobItemManagers(): List<DownloadJobItemManager> {
        return managerMap.values.toList()
    }


    override suspend fun findDownloadJobItemStatusByContentEntryUid(contentEntryUid: Long)  : DownloadJobItemStatus?{
        managerMap.values.forEach {
            val status = it.findStatusByContentEntryUid(contentEntryUid)
            if(status != null)
                return status
        }

        return null
    }

    override fun addDownloadChangeListener(listener: OnDownloadJobItemChangeListener) {
        changeListeners.add(listener)
    }

    override fun removeDownloadChangeListener(listener: OnDownloadJobItemChangeListener) {
        changeListeners.remove(listener)
    }

    fun onDownloadJobItemChange(status: DownloadJobItemStatus?, downloadJobUid: Int) {
        changeListeners.forEach { it.onDownloadJobItemChange(status, downloadJobUid) }

        //if the root item has finished, it no longer needs tracking
        if (status != null && status.status >= JobStatus.COMPLETE_MIN &&
                managerMap.get(downloadJobUid)?.rootContentEntryUid == status.contentEntryUid) {
            managerMap.remove(downloadJobUid)
        }
    }

    fun deleteUnusedDownloadJob(downloadJobUid: Int) {
        val downloadJobManager = managerMap[downloadJobUid]
        val rootItemStatus = downloadJobManager?.rootItemStatus
        if (downloadJobManager != null && rootItemStatus != null) {
            //TODO: fix this
            //downloadJobManager.updateStatus(rootItemStatus.jobItemUid, JobStatus.CANCELED, null)
        }

        if (downloadJobManager != null) {
            managerMap.remove(downloadJobUid)
        }

        appDatabase.downloadJobDao.cleanupUnused(downloadJobUid)
    }

}