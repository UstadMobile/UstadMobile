package com.ustadmobile.core.db.dao

import com.ustadmobile.lib.db.entities.ContentJobItemProgressUpdate

suspend fun ContentJobItemDao.commitProgressUpdates(updates: List<ContentJobItemProgressUpdate>) {
    updates.forEach {
        updateItemProgress(it.cjiUid, it.cjiItemProgress, it.cjiItemTotal)
    }
}