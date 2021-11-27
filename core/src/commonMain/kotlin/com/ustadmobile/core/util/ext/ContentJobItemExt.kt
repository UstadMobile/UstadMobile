package com.ustadmobile.core.util.ext

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.ContentJobItem

fun Int.isStatusPaused() = this == JobStatus.PAUSED

fun Int.isStatusCompleted() = this >= JobStatus.COMPLETE_MIN

fun ContentJobItem?.isStatusQueuedOrDownloading() = this?.cjiRecursiveStatus?.let {it >= JobStatus.WAITING_MIN && it < JobStatus.COMPLETE_MIN } ?: false

fun ContentJobItem?.isStatusPaused() = this?.cjiRecursiveStatus?.isStatusPaused() ?: false

fun ContentJobItem?.isStatusCompletedSuccessfully() = this?.cjiRecursiveStatus?.let {  it == JobStatus.COMPLETE } ?: false

fun ContentJobItem?.isStatusCompleted() = this?.cjiRecursiveStatus?.isStatusCompleted() ?: false

fun ContentJobItem?.isStatusPausedOrQueuedOrDownloading() = this?.cjiRecursiveStatus?.let {it >= JobStatus.PAUSED && it < JobStatus.COMPLETE_MIN } ?: false


/**
 * Set of convenience extension functions that help make status conditions more readable.
 */

private val statusToMessageIdMap = mapOf(
        JobStatus.PAUSED to MessageID.download_entry_state_paused,
        JobStatus.QUEUED to MessageID.queued,
        JobStatus.RUNNING to MessageID.in_progress,
        JobStatus.CANCELLING to MessageID.canceled,
        JobStatus.CANCELED to MessageID.canceled,
        JobStatus.COMPLETE to MessageID.completed,
        JobStatus.FAILED to MessageID.failed,
        JobStatus.DELETED to MessageID.deleted)

private fun Int.downloadJobStatusStr(systemImpl: UstadMobileSystemImpl, context: Any): String {
    val messageId = statusToMessageIdMap[this]
    return if(messageId != null) {
        systemImpl.getString(messageId, context)
    }else {
        ""
    }
}

fun ContentJobItem?.toStatusString(systemImpl: UstadMobileSystemImpl, context: Any)
        = this?.cjiRecursiveStatus?.downloadJobStatusStr(systemImpl, context) ?: ""