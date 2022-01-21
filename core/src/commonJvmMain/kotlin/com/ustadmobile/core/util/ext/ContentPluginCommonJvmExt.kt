package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.*
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.File
import org.kodein.di.DI
import org.kodein.di.on
import org.kodein.di.instance
import org.kodein.di.direct
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.systemTimeInMillis

actual suspend fun deleteFilesForContentJob(
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
                repo.contentEntryDao.invalidateContentEntryCreatedByJob(jobId, true,
                    systemTimeInMillis())
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
            val containerFolder = DoorUri.parse(containerFolderUri).toFile()
            jobItems.forEach {
                val containerUidFolder = File(containerFolder, "${it.cjiContainerUid}")
                val fileList = containerUidFolder.listFiles()
                if (containerUidFolder.exists() && (fileList == null || fileList.size == 0)) {
                    containerUidFolder.delete()
                }
            }
        }

    }

    return numberOfFailedDeletion
}
