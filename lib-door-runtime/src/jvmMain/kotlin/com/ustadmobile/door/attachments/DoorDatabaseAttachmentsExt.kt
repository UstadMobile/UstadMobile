package com.ustadmobile.door.attachments

import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.copyAndGetMd5
import com.ustadmobile.door.ext.toHexString
import com.ustadmobile.door.util.systemTimeInMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI
import java.nio.file.Paths

actual suspend fun DoorDatabaseRepository.storeAttachment(entityWithAttachment: EntityWithAttachment) {
    val attachmentUri = entityWithAttachment.attachmentUri ?: throw IllegalArgumentException("Attachment URI is null!")
    val attachmentDirVal = attachmentsDir ?: throw IllegalStateException("No attachments dir!")

    withContext(Dispatchers.IO) {
        val srcFile = Paths.get(URI(attachmentUri)).toFile()
        val destFile = File(attachmentDirVal, "${systemTimeInMillis()}.tmp")
        val md5 = srcFile.copyAndGetMd5(destFile)

        val entityDir = File(attachmentDirVal, entityWithAttachment.tableName)
        entityDir.takeIf { !it.exists() }?.mkdirs()
        val md5HexStr = md5.toHexString()
        destFile.renameTo(File(entityDir, md5HexStr))

        entityWithAttachment.attachmentMd5 = md5.toHexString()
        entityWithAttachment.attachmentSize = destFile.length().toInt()
        entityWithAttachment.attachmentUri = entityWithAttachment.makeAttachmentUriFromTableNameAndMd5()
    }
}

actual suspend fun DoorDatabaseRepository.retrieveAttachment(uri: String): String {
    val attachmentDirVal = attachmentsDir ?: throw IllegalStateException("No attachments dir!")
    val file = File(attachmentDirVal, uri.substringAfter("door-attachment://"))
    return file.toURI().toString()
}


/**
 * Upload the given attachment uri to the endpoint.
 */
actual suspend fun DoorDatabaseRepository.uploadAttachment(uri: String) {

}

/**
 * Download the given attachment uri from the endpoint
 */
actual suspend fun DoorDatabaseRepository.downloadAttachment(uri: String) {

}


