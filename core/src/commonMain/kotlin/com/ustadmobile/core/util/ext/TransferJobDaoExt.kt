package com.ustadmobile.core.util.ext

import com.ustadmobile.core.db.dao.TransferJobDao
import com.ustadmobile.lib.db.composites.TransferJobItemStatus

suspend fun TransferJobDao.isNotCancelled(jobUid: Int): Boolean {
    return getJobStatus(jobUid) != TransferJobItemStatus.STATUS_CANCELLED
}