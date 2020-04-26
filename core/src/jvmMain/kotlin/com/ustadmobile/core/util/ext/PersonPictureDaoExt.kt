package com.ustadmobile.core.util.ext

import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.lib.db.entities.PersonPicture

actual suspend fun PersonPictureDao.setAttachmentDataFromUri(entity: PersonPicture, uri: String, context: Any) {
    setAttachment(entity, uri)
}

actual fun PersonPictureDao.getAttachmentUri(entity: PersonPicture): String? {
    return getAttachmentPath(entity)
}