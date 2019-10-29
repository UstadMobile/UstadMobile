package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.sharedse.io.FileSe
import kotlinx.coroutines.Runnable

fun deleteDownloadJob(db: UmAppDatabase, rootContentEntryUid: Long, onprogress: (progress: Int) -> Unit) {

    db.runInTransaction(Runnable {

        var downloadJobitem = db.downloadJobItemDao.findByContentEntryUid(rootContentEntryUid)

        db.downloadJobItemDao.forAllChildDownloadJobItemsRecursive(downloadJobitem!!.djiUid) { childItems ->
            childItems.forEach {
                db.containerEntryDao.deleteByContentEntryUid(it.djiContentEntryUid)
                db.contentEntryStatusDao.deleteByContentEntryUid(it.djiContentEntryUid)
            }
        }

        var count = db.containerEntryFileDao.countEntriesByJoin()
        var counter = 0
        do {
            var containerEntryFilesList = db.containerEntryFileDao.findEntriesByJoin()
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