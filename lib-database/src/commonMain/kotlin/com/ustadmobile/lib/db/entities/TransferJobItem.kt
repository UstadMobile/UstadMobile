package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @param tjiSrc: the source of the TransferJobItem - the value of the string depends on the type
 * @param tjiDest: the destination of the TransferJobItem - the value of the string depends on the type
 */
@Entity
data class TransferJobItem(
    @PrimaryKey(autoGenerate = true)
    var tjiUid: Int = 0,

    var tjiTjUid: Int = 0,

    var tjTotalSize: Long = 0,

    var tjTransferred: Long = 0,

    var tjAttemptCount: Int = 0,

    var tjiSrc: String? = null,

    var tjiDest: String? = null,

    var tjiType: Int = 0,
)
