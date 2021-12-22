package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.File
import org.kodein.di.DI
import org.kodein.di.on
import org.kodein.di.instance
import com.ustadmobile.door.ext.DoorTag

actual suspend fun deleteFilesForContentEntry(
        jobId: Long,
        di: DI,
        endpoint: Endpoint
): Int {

    val db:UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)
    val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    var numberOfFailedDeletion = 0
    withContext(Dispatchers.IO) {

        db.runInTransaction {
            // delete all containerEntries for this contentEntry
            db.containerEntryDao.deleteContainerEntriesCreatedByJobs(jobId)
            val contentJobItem = db.contentJobItemDao.findByJobId(jobId)
            if(contentJobItem?.cjiContentDeletedOnCancellation == true){
                repo.contentEntryDao.invalidateContentEntryCreatedByJob(jobId, true)
            }


            var zombieEntryFilesList: List<ContainerEntryFile>
            do {
                zombieEntryFilesList = db.containerEntryFileDao.findZombieEntries()
                zombieEntryFilesList.forEach {
                    val filePath = it.cefPath
                    if (filePath == null || !File(filePath).delete()) {
                        numberOfFailedDeletion++
                    }
                }

                db.containerEntryFileDao.deleteListOfEntryFiles(zombieEntryFilesList)
            } while (zombieEntryFilesList.isNotEmpty())
        }

    }

    return numberOfFailedDeletion
}
