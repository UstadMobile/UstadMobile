package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.ContentPlugin
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.StorageUtil
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

actual suspend fun ContentPlugin.withWifiLock(context: Any, block: suspend () -> Unit) {
    block.invoke()
}

actual suspend fun deleteFilesForContentEntry(contentEntryUid: Long, di: DI, endpoint: Endpoint): Int {
    val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)
    var numberOfFailedDeletion = 0
    withContext(Dispatchers.Unconfined){
        db.runInTransaction(Runnable {
            db.containerEntryDao.deleteByContentEntryUid(contentEntryUid)

            var zombieEntryFilesList: List<ContainerEntryFile>
            do {
                zombieEntryFilesList = db.containerEntryFileDao.findZombieEntries()
                zombieEntryFilesList.forEach {
                    val filePath = it.cefPath
                    if (filePath == null || !StorageUtil.deleteFile(filePath)) {
                        numberOfFailedDeletion++
                    }
                }

                db.containerEntryFileDao.deleteListOfEntryFiles(zombieEntryFilesList)
            } while (zombieEntryFilesList.isNotEmpty())
        })
    }

    return numberOfFailedDeletion
}