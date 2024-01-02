package com.ustadmobile.libcache.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a cache entry.
 *
 * @param cacheFlags flags from the cache-control header.
 */
@Entity
data class CacheEntry(
    @PrimaryKey(autoGenerate = true)
    var ceId: Int = 0,
    @ColumnInfo(index = true)
    var url: String = "",
    var message: String = "",
    var statusCode: Int = 0,
    var cacheFlags: Int = 0,
    var method: Int = 0,
    var lastAccessed: Long = 0,
    var lastValidated: Long = -1,
    @ColumnInfo(index = true)
    var responseBodySha256: String? = null,
    var responseHeaders: String = "",
) {
    companion object {

        const val CACHE_FLAG_STATIC = 8


    }
}