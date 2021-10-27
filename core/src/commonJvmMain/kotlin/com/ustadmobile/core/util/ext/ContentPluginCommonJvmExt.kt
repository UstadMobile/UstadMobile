package com.ustadmobile.core.util.ext

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.torrent.UstadTorrentManager
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import java.io.File

actual suspend fun deleteFilesForContentEntry(
        db: UmAppDatabase,
        contentEntryUid: Long,
        torrentDir: File,
        ustadTorrentManager: UstadTorrentManager): Int{

    var numberOfFailedDeletion = 0
    db.runInTransaction{
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
        val torrentFile = File(torrentDir, "${it.containerUid}.torrent")
        if(torrentFile.exists()){
            torrentFile.delete()
        }
        ustadTorrentManager.removeTorrent(it.containerUid)
    }


    return numberOfFailedDeletion
}
