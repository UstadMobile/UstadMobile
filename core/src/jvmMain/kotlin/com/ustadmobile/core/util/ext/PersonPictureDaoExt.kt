package com.ustadmobile.core.util.ext

import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.lib.db.entities.PersonPicture
import java.io.File

actual suspend fun PersonPictureDao.setAttachmentDataFromUri(entity: PersonPicture, uri: String?, context: Any) {
    if(uri != null) {
        setAttachment(entity, uri)
    }else {
        val emptyUriFile = File.createTempFile("personpicture", "empty")
        setAttachment(entity, emptyUriFile.absolutePath)
        emptyUriFile.delete()
    }

}

actual fun PersonPictureDao.getAttachmentUri(entity: PersonPicture): String? {
    return getAttachmentPath(entity)
}