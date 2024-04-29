package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.TransferJob
import kotlinx.serialization.Serializable

@Serializable
data class TransferJobAndTotals(
    @Embedded
    var transferJob: TransferJob? = null,
    var totalSize: Long = 0,
    var transferred: Long = 0,
    var latestErrorStr: String? = null,
)
