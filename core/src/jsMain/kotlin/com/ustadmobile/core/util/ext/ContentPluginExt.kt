package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.ContentPlugin
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File

actual suspend fun ContentPlugin.withWifiLock(context: Any, block: suspend () -> Unit) {
    block.invoke()
}

actual suspend fun deleteFilesForContentJob(jobId: Long, di: DI, endpoint: Endpoint): Int{
    val db:UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)
    val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)
    var numberOfFailedDeletion = 0
    withContext(Dispatchers.Unconfined){
        db.runInTransaction(Runnable {
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

            val jobItems = db.contentJobItemDao.findAllByJobId(jobId)
            val job = db.contentJobDao.findByUid(jobId)
            val defaultContainerDir: File = di.on(endpoint).direct.instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)
            val containerFolderUri = job?.toUri
                ?: defaultContainerDir.toURI().toString()
            jobItems.forEach {
                //delete all files under containerFolderUri
            }
        })
    }

    return numberOfFailedDeletion
}