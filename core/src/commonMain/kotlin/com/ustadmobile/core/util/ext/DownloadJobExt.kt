package com.ustadmobile.core.util.ext

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.*

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

fun ContentJobItem?.toStatusString(systemImpl: UstadMobileSystemImpl, context: Any)
        = this?.cjiRecursiveStatus?.downloadJobStatusStr(systemImpl, context) ?: ""