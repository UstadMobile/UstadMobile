package com.ustadmobile.door.attachments

import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.DoorDatabaseRepository.Companion.DOOR_ATTACHMENT_URI_PREFIX
import com.ustadmobile.door.ext.copyAndGetMd5
import com.ustadmobile.door.ext.toHexString
import com.ustadmobile.door.util.systemTimeInMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.URI
import java.nio.file.Paths

actual suspend fun DoorDatabaseRepository.storeAttachment(entityWithAttachment: EntityWithAttachment) {
    val attachmentUri = entityWithAttachment.attachmentUri ?: throw IllegalArgumentException("Attachment URI is null!")
    val attachmentDirVal = requireAttachmentDirFile()

    if(attachmentUri.startsWith(DOOR_ATTACHMENT_URI_PREFIX))
        //do nothing - attachment is already stored
        return

    withContext(Dispatchers.IO) {
        val srcFile = Paths.get(URI(attachmentUri)).toFile()
        attachmentDirVal.takeIf { !it.exists() }?.mkdirs()

        val tmpDestFile = File(attachmentDirVal, "${systemTimeInMillis()}.tmp")
        val md5 = srcFile.copyAndGetMd5(tmpDestFile)

        val md5HexStr = md5.toHexString()
        entityWithAttachment.attachmentMd5 = md5.toHexString()
        val finalDestFile = File(requireAttachmentDirFile(), entityWithAttachment.tableNameAndMd5Path)
        if(!tmpDestFile.renameTo(finalDestFile)) {
            throw IOException("Could not move attachment $md5HexStr to it's final file!")
        }

        entityWithAttachment.attachmentSize = tmpDestFile.length().toInt()
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


