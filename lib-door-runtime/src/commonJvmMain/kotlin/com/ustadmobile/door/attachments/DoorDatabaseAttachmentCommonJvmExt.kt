package com.ustadmobile.door.attachments

import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.DoorDatabaseSyncRepository
import java.io.File

fun DoorDatabaseRepository.requireAttachmentDirFile(): File {
    return attachmentsDir?.let { File(it) }
            ?: throw IllegalStateException("requireAttachmentDirFile called on repository with null attachment dir")
}

actual suspend fun DoorDatabaseRepository.deleteZombieAttachments(entityWithAttachment: EntityWithAttachment) {
    //TODO: transaction support for this
    val syncRepo = this as? DoorDatabaseSyncRepository ?: throw IllegalStateException("Database hosting attachments must be syncable")
    val zombieAttachmentDataList = syncRepo.syncHelperEntitiesDao.findZombieAttachments(
            entityWithAttachment.tableName, 0)

    zombieAttachmentDataList.forEach {
        val attachmentFile = File(requireAttachmentDirFile(), it.tableNameAndMd5Path)
        attachmentFile.delete()
    }

    syncRepo.syncHelperEntitiesDao.deleteZombieAttachments(zombieAttachmentDataList)
}
