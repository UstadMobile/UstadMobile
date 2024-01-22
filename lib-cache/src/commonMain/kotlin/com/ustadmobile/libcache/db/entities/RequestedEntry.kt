package com.ustadmobile.libcache.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Used to make things more efficient when working with batch requests.
 */
@Entity
data class RequestedEntry(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var requestSha256: String = "",
    var requestedKey: String = "",
    @ColumnInfo(index = true)
    var batchId: Int = 0,
) {
}