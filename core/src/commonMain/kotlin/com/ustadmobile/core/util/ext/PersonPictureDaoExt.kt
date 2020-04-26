package com.ustadmobile.core.util.ext

import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.lib.db.entities.PersonPicture

expect suspend fun PersonPictureDao.setAttachmentDataFromUri(entity: PersonPicture, uri: String, context: Any)

expect fun PersonPictureDao.getAttachmentUri(entity: PersonPicture): String?
