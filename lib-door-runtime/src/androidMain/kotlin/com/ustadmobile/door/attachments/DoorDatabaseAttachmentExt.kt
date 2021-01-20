package com.ustadmobile.door.attachments

import android.content.Context
import android.net.Uri
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.toHexString
import com.ustadmobile.door.ext.writeToFileAndGetMd5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


fun DoorDatabaseRepository.requireAttachmentDirFile(): File {
    return attachmentsDir?.let { File(it) }
            ?: throw IllegalStateException("requireAttachmentDirFile called on repository with null attachment dir")
}


actual suspend fun DoorDatabaseRepository.storeAttachment(entityWithAttachment: EntityWithAttachment) {
    val androidContext = context as Context
    if(entityWithAttachment.attachmentUri?.startsWith("door-attachment://") == true)
        return //already stored

    withContext(Dispatchers.IO) {
        val attachmentsDir = requireAttachmentDirFile()
        attachmentsDir.takeIf { !it.exists() }?.mkdirs()

        val androidUri = Uri.parse(entityWithAttachment.attachmentUri)
        val inStream = androidContext.contentResolver.openInputStream(androidUri) ?: throw IOException("No input stream for $androidUri")
        val tmpDestFile = File(attachmentsDir, "${System.currentTimeMillis()}.tmp")
        val md5 = inStream.writeToFileAndGetMd5(tmpDestFile)

        val finalDestFile = File(requireAttachmentDirFile(), entityWithAttachment.relativePath)
        finalDestFile.parentFile?.takeIf { !it.exists() }?.mkdir()
        tmpDestFile.renameTo(finalDestFile)

        entityWithAttachment.attachmentMd5 = md5.toHexString()
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

