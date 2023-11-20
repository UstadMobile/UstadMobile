package com.ustadmobile.libcache.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a stored response body
 *
 * @param the sha256 sum
 * @param storageUri - the file (or other URI) where the body is stored
 */
@Entity
data class ResponseBody(
    @PrimaryKey(autoGenerate = true)
    var responseId: Int = 0,
    var sha256: String = "",
    var storageUri: String = "",
)
