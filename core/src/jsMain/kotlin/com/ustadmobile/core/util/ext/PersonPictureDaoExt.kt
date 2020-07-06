package com.ustadmobile.core.util.ext

import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.lib.db.entities.PersonPicture

actual suspend fun PersonPictureDao.setAttachmentDataFromUri(entity: PersonPicture, uri: String?, context: Any) {
    TODO("Not Implemented")
}

actual fun PersonPictureDao.getAttachmentUri(entity: PersonPicture): String? {
    TODO("Not implemented on JS")
}
