package com.ustadmobile.core.util.ext

import com.ustadmobile.core.contentjob.ContentJobProgressListener
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.io.ext.getSize
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentJobItem
import org.kodein.di.DI
import kotlin.jvm.JvmStatic

fun Int.isStatusPaused() = this == JobStatus.PAUSED

fun Int.isStatusCompleted() = this >= JobStatus.COMPLETE_MIN

fun Int.isStatusActiveOrQueued() = (this >= JobStatus.QUEUED && this < JobStatus.COMPLETE_MIN)

fun ContentJobItem?.isStatusQueuedOrDownloading() = this?.cjiRecursiveStatus?.let {it >= JobStatus.WAITING_MIN && it < JobStatus.COMPLETE_MIN } ?: false

fun ContentJobItem?.isStatusPaused() = this?.cjiRecursiveStatus?.isStatusPaused() ?: false

fun ContentJobItem?.isStatusCompletedSuccessfully() = this?.cjiRecursiveStatus?.let {  it == JobStatus.COMPLETE } ?: false

fun ContentJobItem?.isStatusCompleted() = this?.cjiRecursiveStatus?.isStatusCompleted() ?: false

fun ContentJobItem?.isStatusPausedOrQueuedOrDownloading() = this?.cjiRecursiveStatus?.let {it >= JobStatus.PAUSED && it < JobStatus.COMPLETE_MIN } ?: false


/**
 * Set of convenience extension functions that help make status conditions more readable.
 */

private val statusToMessageIdMap = mapOf(
        JobStatus.PAUSED to MR.strings.download_entry_state_paused,
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

fun ContentJobItem?.toStatusString(systemImpl: UstadMobileSystemImpl, context: Any)
        = this?.cjiRecursiveStatus?.downloadJobStatusStr(systemImpl, context) ?: ""

/**
 * Used by ContentPlugins when an import type job starts. Updates the size of this job according to
 * the local uri (using getSize extension function on DoorUri).
 */
suspend fun ContentJobItem.updateTotalFromLocalUriIfNeeded(
    uri: DoorUri,
    uploadNeeded: Boolean,
    progressListener: ContentJobProgressListener?,
    context: Any,
    di: DI
) {
    if(cjiItemTotal == 0L) {
        cjiItemTotal = uri.getSize(context, di)
        if(uploadNeeded)
            cjiItemTotal *= 2

        progressListener?.onProgress(this)
    }
}

/**
 * Used by ContentPlugins after a container has been generated. updateTotalFromLocalUriIfNeeded
 * will set the size using the size of the original file. This updates it according to the size
 * of the container.
 */
suspend fun ContentJobItem.updateTotalFromContainerSize(
    uploadNeeded: Boolean,
    db: UmAppDatabase,
    progressListener: ContentJobProgressListener
) {
    val containerSize = db.containerDao.findSizeByUid(cjiContainerUid)
    cjiItemTotal = if(uploadNeeded) {
        containerSize * 2
    } else {
        containerSize
    }

    cjiItemProgress = containerSize

    progressListener.onProgress(this)
}

