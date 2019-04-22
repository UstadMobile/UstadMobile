package com.ustadmobile.core.networkmanager

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmResultCallback
import com.ustadmobile.lib.db.entities.DownloadJob
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus
import java.util.concurrent.atomic.AtomicInteger

/**
 * Manages a list of DownloadJobItemManagers. Creates new items, closes downloadjobs that have finished etc.
 */
class DownloadJobItemManagerList(private val appDatabase: UmAppDatabase) : DownloadJobItemStatusProvider, DownloadJobItemManager.OnDownloadJobItemChangeListener{

    private val managerMap = mutableMapOf<Int, DownloadJobItemManager>()

    private val changeListeners = mutableListOf<DownloadJobItemManager.OnDownloadJobItemChangeListener>()

    fun createNewDownloadJobItemManager(newDownloadJob: DownloadJob) : DownloadJobItemManager {
        newDownloadJob.djUid = appDatabase.downloadJobDao.insert(newDownloadJob)
        val manager = DownloadJobItemManager(appDatabase, newDownloadJob.djUid.toInt())
        manager.onDownloadJobItemChangeListener = this
        managerMap.put(newDownloadJob.djUid.toInt(), manager)

        return manager
    }

    fun getDownloadJobItemManager(downloadJobId: Int): DownloadJobItemManager? {
        return managerMap.get(downloadJobId)
    }

    override fun findDownloadJobItemStatusByContentEntryUid(contentEntryUid: Long, callback: UmResultCallback<DownloadJobItemStatus>?) {
        if(managerMap.isEmpty()){
            callback?.onDone(null)
            return
        }

        val managerListToCheck = managerMap.values.toList()
        val checksLeft = AtomicInteger(managerListToCheck.size)

        for(manager in managerListToCheck) {
            manager.findStatusByContentEntryUid(contentEntryUid, object: UmResultCallback<DownloadJobItemStatus> {
                override fun onDone(result: DownloadJobItemStatus?) {
                    if(result != null) {
                        callback?.onDone(result)
                        checksLeft.set(-1)
                    }else if(checksLeft.decrementAndGet() == 0) {
                        callback?.onDone(null)
                    }
                }
            })
        }
    }

    override fun addDownloadChangeListener(listener: DownloadJobItemManager.OnDownloadJobItemChangeListener) {
        synchronized(changeListeners) {
            changeListeners.add(listener)
        }
    }

    override fun removeDownloadChangeListener(listener: DownloadJobItemManager.OnDownloadJobItemChangeListener) {
        synchronized(changeListeners) {
            changeListeners.remove(listener)
        }
    }

    override fun onDownloadJobItemChange(status: DownloadJobItemStatus?) {
        var listenersToNotify : List<DownloadJobItemManager.OnDownloadJobItemChangeListener>
        synchronized(changeListeners) {
            listenersToNotify = changeListeners.toList()
        }

        for(listener in listenersToNotify) {
            listener.onDownloadJobItemChange(status)
        }
    }


}