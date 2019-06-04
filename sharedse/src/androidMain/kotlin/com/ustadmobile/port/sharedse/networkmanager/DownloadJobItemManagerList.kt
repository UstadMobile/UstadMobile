package com.ustadmobile.port.sharedse.networkmanager

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmResultCallback
import com.ustadmobile.core.networkmanager.DownloadJobItemStatusProvider
import com.ustadmobile.core.networkmanager.OnDownloadJobItemChangeListener
import com.ustadmobile.lib.db.entities.DownloadJob
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus
import java.util.concurrent.atomic.AtomicInteger

/**
 * Manages a list of DownloadJobItemManagers. Creates new items, closes downloadjobs that have finished etc.
 */
class DownloadJobItemManagerList(private val appDatabase: UmAppDatabase) : DownloadJobItemStatusProvider, OnDownloadJobItemChangeListener {


    private val managerMap = mutableMapOf<Int, DownloadJobItemManager>()

    private val changeListeners = mutableListOf<OnDownloadJobItemChangeListener>()

    fun createNewDownloadJobItemManager(newDownloadJob: DownloadJob): DownloadJobItemManager {
        newDownloadJob.djUid = appDatabase.downloadJobDao.insert(newDownloadJob).toInt()
        val manager = DownloadJobItemManager(appDatabase, newDownloadJob.djUid)
        manager.onDownloadJobItemChangeListener = this
        managerMap[newDownloadJob.djUid.toInt()] = manager

        return manager
    }

    fun getDownloadJobItemManager(downloadJobId: Int): DownloadJobItemManager? {
        return managerMap.get(downloadJobId)
    }

    fun getActiveDownloadJobItemManagers(): List<DownloadJobItemManager> {
        return managerMap.values.toList()
    }


    override fun findDownloadJobItemStatusByContentEntryUid(contentEntryUid: Long, callback: UmResultCallback<DownloadJobItemStatus?>){
        if (managerMap.isEmpty()) {
            callback.onDone(null)
            return
        }

        val managerListToCheck = managerMap.values.toList()
        val checksLeft = AtomicInteger(managerListToCheck.size)

        for (manager in managerListToCheck) {
            manager.findStatusByContentEntryUid(contentEntryUid, object : UmResultCallback<DownloadJobItemStatus> {
                override fun onDone(result: DownloadJobItemStatus?) {
                    if (result != null) {
                        callback.onDone(result)
                        checksLeft.set(-1)
                    } else if (checksLeft.decrementAndGet() == 0) {
                        callback.onDone(null)
                    }
                }
            })
        }
    }

    override fun addDownloadChangeListener(listener: OnDownloadJobItemChangeListener) {
        synchronized(changeListeners) {
            changeListeners.add(listener)
        }
    }

    override fun removeDownloadChangeListener(listener: OnDownloadJobItemChangeListener) {
        synchronized(changeListeners) {
            changeListeners.remove(listener)
        }
    }

    override fun onDownloadJobItemChange(status: DownloadJobItemStatus?, downloadJobUid: Int) {
        var listenersToNotify: List<OnDownloadJobItemChangeListener>
        synchronized(changeListeners) {
            listenersToNotify = changeListeners.toList()
        }

        for (listener in listenersToNotify) {
            listener.onDownloadJobItemChange(status, downloadJobUid)
        }

        if (status != null && status.status >= JobStatus.COMPLETE_MIN &&
                managerMap.get(downloadJobUid)?.rootContentEntryUid == status.contentEntryUid) {
            synchronized(managerMap) {
                managerMap.remove(downloadJobUid)
            }
        }
    }

    fun deleteUnusedDownloadJob(downloadJobUid: Int) {
        val downloadJobManager = managerMap[downloadJobUid]
        val rootItemStatus = downloadJobManager?.rootItemStatus
        if (downloadJobManager != null && rootItemStatus != null) {
            downloadJobManager.updateStatus(rootItemStatus.jobItemUid, JobStatus.CANCELED, null)
        }

        if (downloadJobManager != null) {
            managerMap.remove(downloadJobUid)
        }

        appDatabase.downloadJobDao.cleanupUnused(downloadJobUid)
    }

}