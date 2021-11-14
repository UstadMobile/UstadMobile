package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.torrent.UstadTorrentManager
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.door.DoorUri
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.File
import org.kodein.di.DI
import org.kodein.di.on
import org.kodein.di.instance
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.ext.DoorTag

actual suspend fun deleteFilesForContentEntry(
        contentEntryUid: Long,
        di: DI,
        endpoint: Endpoint
): Int {

    val torrentDirFile: File by di.on(endpoint).instance()
    val db:UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)
    val torrentManager: UstadTorrentManager by di.on(endpoint).instance()


    var numberOfFailedDeletion = 0
    withContext(Dispatchers.IO) {

        db.runInTransaction {
            // delete all containerEntries for this contentEntry
            db.containerEntryDao.deleteByContentEntryUid(contentEntryUid)

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

        val containers = db.containerDao.findContainersForContentEntryUid(contentEntryUid)

        containers.forEach {
            torrentManager.removeTorrent(it.containerUid)
            val torrentFile = File(torrentDirFile, "${it.containerUid}.torrent")
            if(torrentFile.exists()){
                torrentFile.delete()
            }
        }
    }

    return numberOfFailedDeletion
}
