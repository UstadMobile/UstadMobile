package com.ustadmobile.door.attachments

import com.github.aakira.napier.Napier
import com.ustadmobile.door.DoorConstants
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.DoorDatabaseRepository.Companion.DOOR_ATTACHMENT_URI_PREFIX
import com.ustadmobile.door.ext.*
import com.ustadmobile.door.util.systemTimeInMillis
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder
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
        finalDestFile.parentFile.takeIf { !it.exists() }?.mkdirs()

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

