package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.lib.db.entities.DownloadJobItem
import com.ustadmobile.sharedse.io.FileSe
import kotlinx.coroutines.Runnable

expect fun requestDelete(downloadJobUid: Int, containerDownloadManager: ContainerDownloadManager,
                         context: Any)

suspend fun deleteDownloadJob(db: UmAppDatabase, downloadJobUid: Int,
                      containerDownloadManager: ContainerDownloadManager,
                      onprogress: (progress: Int) -> Unit): Boolean {

    val downloadJob = db.downloadJobDao.findByUid(downloadJobUid)

    if(downloadJob == null) {
        return false
    }

    db.downloadJobItemDao.forAllChildDownloadJobItemsRecursiveAsync(downloadJobUid) { childItems ->
        childItems.forEach {
            db.containerEntryDao.deleteByContentEntryUid(it.djiContentEntryUid)

            containerDownloadManager.handleDownloadJobItemUpdated(DownloadJobItem(it).also {
                it.djiStatus = JobStatus.DELETED
            }, autoCommit = false)
        }
    }

    containerDownloadManager.commit()

    var numFailures = 0
    db.runInTransaction(Runnable {
        var counter = 0
        val zombieEntryFilesList = db.containerEntryFileDao.findZombieEntries()
        zombieEntryFilesList.forEach {
            val filePath = it.cefPath
            if(filePath == null || !FileSe(filePath).delete()) {
                numFailures++
            }
        }
        onprogress.invoke(100)

        db.containerEntryFileDao.deleteListOfEntryFiles(zombieEntryFilesList)
    })

    return numFailures == 0
}