package com.ustadmobile.libcache.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a stored response body
 *
 * @param the sha256 sum of the data
 * @param storageUri - the file (or other URI) where the body is stored
 * @param bodySize the size as stored on the disk
 */
@Entity
data class ResponseBody(
    @PrimaryKey(autoGenerate = true)
    var responseId: Int = 0,
    var sha256: String = "",
    var storageUri: String = "",
    @ColumnInfo(defaultValue = "0")
    var bodySize: Long = 0,
)
