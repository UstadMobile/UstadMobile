package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.OfflineItem
import kotlinx.serialization.Serializable

/**
 * Represents the information required to show the offline state of a given item e.g. ContentEntry,
 * Course, etc.
 */
@Serializable
data class OfflineItemAndState (
    @Embedded
    var offlineItem: OfflineItem? = null,
    @Embedded
    var activeDownload: TransferJobAndTotals? = null,
    var readyForOffline: Boolean = false,
)