package com.ustadmobile.core.util.ext

import com.ustadmobile.core.contentformats.ContentImportProgressListener
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentEntryImportJob


/**
 * Set of convenience extension functions that help make status conditions more readable.
 */

private val statusToMessageIdMap = mapOf(
        JobStatus.QUEUED to MR.strings.queued,
        JobStatus.RUNNING to MR.strings.in_progress,
        JobStatus.WAITING_FOR_CONNECTION to MR.strings.waiting,
        JobStatus.CANCELED to MR.strings.canceled,
        JobStatus.COMPLETE to MR.strings.completed,
        JobStatus.FAILED to MR.strings.failed)

private fun Int.downloadJobStatusStr(systemImpl: UstadMobileSystemImpl, context: Any): String {
    val messageId = statusToMessageIdMap[this]
    return if(messageId != null) {
        systemImpl.getString(messageId)
    }else {
        ""
    }
}

fun ContentEntryImportJob?.toStatusString(systemImpl: UstadMobileSystemImpl, context: Any)
        = this?.cjiRecursiveStatus?.downloadJobStatusStr(systemImpl, context) ?: ""

/**
 * Used by ContentPlugins after a container has been generated. updateTotalFromLocalUriIfNeeded
 * will set the size using the size of the original file. This updates it according to the size
 * of the container.
 */
suspend fun ContentEntryImportJob.updateTotalFromContainerSize(
    uploadNeeded: Boolean,
    db: UmAppDatabase,
    progressListener: ContentImportProgressListener
) {
    val containerSize = db.containerDao.findSizeByUid(cjiContentEntryVersion)
    cjiItemTotal = if(uploadNeeded) {
        containerSize * 2
    } else {
        containerSize
    }

    cjiItemProgress = containerSize

    progressListener.onProgress(this)
}

fun ContentEntryImportJob?.requireSourceAsDoorUri(): DoorUri {
    return this?.sourceUri?.let { DoorUri.parse(it) }
        ?: throw IllegalArgumentException("requireSourceAsDoorUri: SourceUri is null!")
}

