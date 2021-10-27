package com.ustadmobile.core.util.ext

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.torrent.UstadTorrentManager
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.door.DoorUri
import java.io.File

actual suspend fun deleteFilesForContentEntry(
        db: UmAppDatabase,
        contentEntryUid: Long,
        torrentDir: DoorUri,
        ustadTorrentManager: UstadTorrentManager): Int{

    val torrentDirFile: File = torrentDir.toFile()

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
        val torrentFile = File(torrentDirFile, "${it.containerUid}.torrent")
        if(torrentFile.exists()){
            torrentFile.delete()
        }
        ustadTorrentManager.removeTorrent(it.containerUid)
    }


    return numberOfFailedDeletion
}
