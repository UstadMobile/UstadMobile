package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    indices = arrayOf(
        Index("tjeTjUid", name = "idx_transferjoberror_tjetjuid")
    )
)
@Serializable
data class TransferJobError(
    @PrimaryKey(autoGenerate = true)
    var tjeId: Int = 0,
    var tjeTjUid: Int = 0,
    var tjeTime: Long = 0,
    var tjeErrorStr: String? = null,
    var tjeDismissed: Boolean = false,
)
