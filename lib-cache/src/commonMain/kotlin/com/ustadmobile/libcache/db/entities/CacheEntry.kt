package com.ustadmobile.libcache.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a cache entry.
 *
 * @param cacheFlags flags from the cache-control header.
 * @param freshUntil the time (in ms) that this entry can be considered fresh, and therefor
 *        reused without subsequent requests.
 * @param lastModified as per the most recently received last-modified header. This will be used if
 *        revalidation is required.
 * @param etag the most recently received etag header. This will be used if revalidation is required.
 */
@Entity
data class CacheEntry(
    @PrimaryKey(autoGenerate = true)
    var ceId: Int = 0,
    @ColumnInfo(index = true)
    var url: String = "",
    var method: Int = 0,
    var cacheFlags: Int = 0,
    var freshUntil: Long = 0,
    var lastAccessed: Long = 0,
    var lastModified: Long = -1,
    var etag: String? = null,
    @ColumnInfo(index = true)
    var responseBodySha256: String? = null,
    var responseHeaders: String = "",
) {
    companion object {

        const val CACHE_FLAG_MUST_REVALIDATE = 1

        const val CACHE_FLAG_PRIVATE = 2

        const val CACHE_FLAG_ONLY_IF_CACHED = 4

    }
}