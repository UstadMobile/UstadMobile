package com.ustadmobile.core.util.ext

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.DownloadJob
import com.ustadmobile.lib.db.entities.DownloadJobItem
import com.ustadmobile.lib.db.entities.DownloadJobItemWithParents

/**
 * Set of convenience extension functions that help make status conditions more readable.
 */

private val statusToMessageIdMap = mapOf(
        JobStatus.PAUSED to MessageID.download_entry_state_paused,
        JobStatus.QUEUED to MessageID.queued,
        JobStatus.RUNNING to MessageID.downloading,
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

fun DownloadJob?.toStatusString(systemImpl: UstadMobileSystemImpl, context: Any)
        = this?.djStatus?.downloadJobStatusStr(systemImpl, context) ?: ""

fun DownloadJobItem?.toStatusString(systemImpl: UstadMobileSystemImpl, context: Any)
        = this?.djiStatus?.downloadJobStatusStr(systemImpl, context) ?: ""

/**
 * Make the root DownloadJobItem for the given DownloadJob
 */
fun DownloadJob.makeRootDownloadJobItem(rootContainer: Container?): DownloadJobItemWithParents {
    return DownloadJobItemWithParents(
            this, this.djRootContentEntryUid,
            rootContainer?.containerUid ?: 0,
            rootContainer?.fileSize ?: 0,
            mutableListOf())
}