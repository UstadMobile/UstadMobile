package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.sharedse.io.FileSe
import kotlinx.coroutines.Runnable

expect fun requestDelete(contentEntryUid: Long, context: Any)

fun deleteDownloadJob(db: UmAppDatabase, rootContentEntryUid: Long, onprogress: (progress: Int) -> Unit) {

    db.runInTransaction(Runnable {

        var rootDownloadJobItem = db.downloadJobItemDao.findByContentEntryUid(rootContentEntryUid)

        var downloadJob = db.downloadJobDao.findByUid(rootDownloadJobItem!!.djiDjUid)

        if (downloadJob!!.djRootContentEntryUid == rootContentEntryUid) {
            db.downloadJobDao.changeStatus(JobStatus.DELETED, rootDownloadJobItem.djiDjUid)
        }

        db.downloadJobItemDao.forAllChildDownloadJobItemsRecursive(rootDownloadJobItem.djiUid) { childItems ->
            childItems.forEach {
                db.containerEntryDao.deleteByContentEntryUid(it.djiContentEntryUid)
                db.contentEntryStatusDao.deleteByContentEntryUid(it.djiContentEntryUid)
                db.downloadJobItemDao.updateStatus(JobStatus.DELETED, it.djiUid)
            }
        }

        val count = db.containerEntryFileDao.countZombieEntries()
        if (count == 0) {
            onprogress.invoke(100)
            return@Runnable
        }
        var counter = 0
        do {
            var containerEntryFilesList = db.containerEntryFileDao.findZombieEntries()
            containerEntryFilesList.forEach {
                var file = FileSe(it.cefPath!!)
                file.delete()
                counter++
            }
            onprogress.invoke((counter * 100) / count)

            db.containerEntryFileDao.deleteListOfEntryFiles(containerEntryFilesList)

        } while (containerEntryFilesList.isNotEmpty())


    })

}