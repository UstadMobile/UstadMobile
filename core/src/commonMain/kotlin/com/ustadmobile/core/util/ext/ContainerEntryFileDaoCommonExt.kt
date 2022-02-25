package com.ustadmobile.core.util.ext

import com.ustadmobile.core.db.dao.ContainerEntryFileDao
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.lib.db.entities.ContainerEntryFileUidAndPath
import java.lang.Integer


/**
 * WARNING: This must be used in a transaction!
 *
 * This will delete the ContainerEntryFile and the entity for any containerEntryFile that is not
 * linked to a ContainerEntry.
 *
 * @return List of ContainerEntries identified as zombies that have not been deleted
 */
suspend fun ContainerEntryFileDao.deleteZombieContainerEntryFiles(
    dbType: Int
) : List<ContainerEntryFileUidAndPath> {
    val notDeleted = mutableListOf<ContainerEntryFileUidAndPath>()
    do {
        val zombies = findZombieUidsAndPath(1000)
        val maxChunkSize = if(dbType == DoorDbType.SQLITE) {
            100
        }else {
            Integer.MAX_VALUE
        }

        val deletedZombies = deleteContainerEntryFilePaths(zombies)

        val entriesToDeleteFromDb = if(deletedZombies.second.isEmpty()) {
            deletedZombies.first
        }else {
            deletedZombies.first + deletedZombies.second
        }
        entriesToDeleteFromDb.chunked(maxChunkSize).forEach {
            deleteByUidList(it.map { it.cefUid } )
        }

        notDeleted += deletedZombies.second
    }while(zombies.isNotEmpty())

    return notDeleted
}
