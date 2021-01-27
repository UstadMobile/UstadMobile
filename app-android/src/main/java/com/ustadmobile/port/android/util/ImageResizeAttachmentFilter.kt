package com.ustadmobile.port.android.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.ustadmobile.door.attachments.AttachmentFilter
import com.ustadmobile.door.attachments.EntityWithAttachment
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.door.util.systemTimeInMillis
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.destination
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.resolution
import java.io.File
import java.io.IOException

class ImageResizeAttachmentFilter(val tableName: String, val width: Int, val height: Int) : AttachmentFilter {

    override suspend fun filter(entityWithAttachment: EntityWithAttachment, tmpDir: String, context: Any): EntityWithAttachment {
        if(entityWithAttachment.tableName != tableName)
            return entityWithAttachment

        val tmpOut = File(tmpDir, "${System.currentTimeMillis()}.tmp")
        val androidUri = Uri.parse(entityWithAttachment.attachmentUri)
        val androidContext = context as Context

        val fileToCompress = if(androidUri.scheme == "file") {
            androidUri.toFile()
        }else {
            val uriInput = androidContext.contentResolver.openInputStream(androidUri)
                    ?: throw IOException("Could not get InputStream for $androidUri")
            File(tmpDir, "${systemTimeInMillis()}.in.tmp").also {
                uriInput.writeToFile(it)
            }
        }

        Compressor.compress(androidContext, fileToCompress) {
            format(Bitmap.CompressFormat.WEBP)
            resolution(width, height)
            destination(tmpOut)
        }

        entityWithAttachment.attachmentUri = tmpOut.toUri().toString()
        return entityWithAttachment
    }
}