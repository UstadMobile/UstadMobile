package com.ustadmobile.door.attachments

import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.DoorDatabaseSyncRepository
import com.ustadmobile.door.ext.dbVersionHeader
import io.ktor.client.request.*
import io.ktor.http.*
import java.io.File
import java.net.URL
import io.ktor.client.content.LocalFileContent

fun DoorDatabaseRepository.requireAttachmentDirFile(): File {
    return attachmentsDir?.let { File(it) }
            ?: throw IllegalStateException("requireAttachmentDirFile called on repository with null attachment dir")
}

/**
 * Upload the given attachment uri to the endpoint.
 */
actual suspend fun DoorDatabaseRepository.uploadAttachment(entityWithAttachment: EntityWithAttachment) {
    val attachmentUri = entityWithAttachment.attachmentUri
            ?: throw IllegalArgumentException("uploadAttachment: Entity with attachment uri must not be null")
    val attachmentMd5 = entityWithAttachment.attachmentMd5
            ?: throw IllegalArgumentException("uploadAttachment: Entity attachment must not be null")

    val attachmentFile = File(requireAttachmentDirFile(), attachmentUri.substringAfter(DoorDatabaseRepository.DOOR_ATTACHMENT_URI_PREFIX))
    val endpointUrl = URL(URL(this.endpoint), "$dbPath/attachments/upload")

    //val inputFile = Paths.get(systemUri).toFile()
    httpClient.post<Unit>(endpointUrl.toString()) {
        dbVersionHeader(db)
        parameter("md5", attachmentMd5)
        parameter("uri", attachmentUri)

        body = LocalFileContent(file = attachmentFile, contentType = ContentType.Application.OctetStream)
    }
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
