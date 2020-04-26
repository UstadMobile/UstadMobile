package com.ustadmobile.core.util.ext

import android.content.Context
import android.net.Uri
import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.lib.db.entities.PersonPicture
import java.io.File
import java.io.FileNotFoundException

actual suspend fun PersonPictureDao.setAttachmentDataFromUri(entity: PersonPicture, uri: String?, context: Any) {
    val androidUri = if(uri != null) Uri.parse(uri) else null
    if(uri != null && androidUri?.scheme == "file") {
        setAttachment(entity, uri)
    }else {
        val tmpFile = File.createTempFile("personDao", "image")
        if(androidUri != null){
            val input = (context as Context).contentResolver.openInputStream(androidUri) ?: return
            val output = tmpFile.outputStream()
            input.copyTo(tmpFile.outputStream())
            output.close()
            input.close()
        }
        setAttachment(entity, tmpFile.absolutePath)
    }
}

actual fun PersonPictureDao.getAttachmentUri(entity: PersonPicture): String? {
    val filePath = getAttachmentPath(entity) ?: return null
    return Uri.fromFile(File(filePath)).toString()
}
