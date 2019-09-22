package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.DownloadJobItemStatusProvider
import com.ustadmobile.core.networkmanager.OnDownloadJobItemChangeListener
import com.ustadmobile.lib.db.entities.DownloadJob
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus
import com.ustadmobile.lib.util.copyOnWriteListOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Manages a list of DownloadJobItemManagers. Creates new items, closes downloadjobs that have finished etc.
 */
class DownloadJobItemManagerList(private val appDatabase: UmAppDatabase,
                                 private val coroutineDispatcher: CoroutineDispatcher) : DownloadJobItemStatusProvider {


    private val managerMap = mutableMapOf<Int, DownloadJobItemManager>()

    private val changeListeners = copyOnWriteListOf<OnDownloadJobItemChangeListener>()

    val activeDownloadJobItemManagers
        get() = managerMap.values.toList()

    private val mutex = Mutex()

    suspend fun createNewDownloadJobItemManager(newDownloadJob: DownloadJob): DownloadJobItemManager {
        mutex.withLock {
            newDownloadJob.djUid = appDatabase.downloadJobDao.insert(newDownloadJob).toInt()
            val manager = DownloadJobItemManager(appDatabase, newDownloadJob.djUid,
                    coroutineDispatcher)
            manager.onDownloadJobItemChangeListener = this::onDownloadJobItemChange
            managerMap[newDownloadJob.djUid] = manager

            return manager
        }
    }

    /**
     * Will return a downloadmanager IF and only if it is currently active and in the memory,
     * otherwise returns null
     *
     * @param downloadJobId ID of the download job to load
     */
    fun getDownloadJobItemManager(downloadJobId: Int): DownloadJobItemManager? {
        return managerMap.get(downloadJobId)
    }

    /**
     * Will open a downloadjobitemmanager. If the downloadjobitemmanager for this downloadjob
     * has already been loaded, it will be returned. Otherwise the downloadjob will be loaded
     * from the database
     */
    suspend fun openDownloadJobItemManager(downloadJobId: Int): DownloadJobItemManager? {
        return mutex.withLock {
            var downloadJobItemManager = getDownloadJobItemManager(downloadJobId)
            if(downloadJobItemManager == null) {
                val downloadJob = appDatabase.downloadJobDao.findByUid(downloadJobId) ?: return null

                downloadJobItemManager = DownloadJobItemManager(appDatabase, downloadJobId,
                        coroutineDispatcher)
                managerMap[downloadJob.djUid] = downloadJobItemManager
            }

            downloadJobItemManager
        }
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

    suspend fun deleteUnusedDownloadJob(downloadJobUid: Int) {
        mutex.withLock {
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

}