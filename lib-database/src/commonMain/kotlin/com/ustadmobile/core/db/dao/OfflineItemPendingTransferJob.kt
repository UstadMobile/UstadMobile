package com.ustadmobile.core.db.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an item which needs downloaded by the client because of an active OfflineItem. This is
 * created by triggers and then processed by OfflineItemsDownloadEnqueuer
 *
 * @param oiptjOiUid id of the related OfflineItem
 * @param oiptjTableId table id
 * @param oiptjEntityUid entity uid
 * @param oiptjUrl url to download (optional).
 * @param oiptjType transfer type - e.g. triggered update or created by user demand. This can
 *        influence use of metered networks etc.
 */
@Entity
data class OfflineItemPendingTransferJob(
    @PrimaryKey(autoGenerate = true)
    var oiptjId: Int = 0,
    var oiptjOiUid: Long = 0,
    var oiptjTableId: Int = 0,
    var oiptjEntityUid: Long = 0,
    var oiptjUrl: String? = null,
    var oiptjType: Int = 0,
)

