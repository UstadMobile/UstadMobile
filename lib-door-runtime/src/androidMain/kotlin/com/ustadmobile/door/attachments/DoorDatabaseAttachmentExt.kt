package com.ustadmobile.door.attachments

import android.content.Context
import android.net.Uri
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.toHexString
import com.ustadmobile.door.ext.writeToFileAndGetMd5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException


actual suspend fun DoorDatabaseRepository.storeAttachment(entityWithAttachment: EntityWithAttachment) {
    val androidContext = context as Context
    if(entityWithAttachment.attachmentUri?.startsWith(DoorDatabaseRepository.DOOR_ATTACHMENT_URI_PREFIX) == true)
        return //already stored

    withContext(Dispatchers.IO) {
        val attachmentsDir = requireAttachmentDirFile()
        attachmentsDir.takeIf { !it.exists() }?.mkdirs()

        val androidUri = Uri.parse(entityWithAttachment.attachmentUri)
        val inStream = androidContext.contentResolver.openInputStream(androidUri) ?: throw IOException("No input stream for $androidUri")
        val tmpDestFile = File(attachmentsDir, "${System.currentTimeMillis()}.tmp")
        val md5 = inStream.writeToFileAndGetMd5(tmpDestFile)
        entityWithAttachment.attachmentMd5 = md5.toHexString()

        val finalDestFile = File(requireAttachmentDirFile(), entityWithAttachment.tableNameAndMd5Path)
        finalDestFile.parentFile?.takeIf { !it.exists() }?.mkdir()
        if(!tmpDestFile.renameTo(finalDestFile)) {
            throw IOException("Unable to move attachment to correct destination")
        }

        entityWithAttachment.attachmentUri = entityWithAttachment.makeAttachmentUriFromTableNameAndMd5()
        entityWithAttachment.attachmentSize = finalDestFile.length().toInt()
    }
}

actual suspend fun DoorDatabaseRepository.retrieveAttachment(uri: String): String {
    val file = File(requireAttachmentDirFile(), uri.substringAfter("door-attachment://"))
    return Uri.fromFile(file).toString()
}

actual suspend fun DoorDatabaseRepository.uploadAttachment(uri: String) {

}

actual suspend fun DoorDatabaseRepository.downloadAttachment(uri: String) {

}

