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
    /**
     * The key is the md5 of the URL (base64 encoded). This creates a unique key based on the URL,
     * and improves performance when searching/indexing because it is shorter than the url itself.
     */
    @PrimaryKey
    var key: String = "",

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

    /**
     * The path where the body of the request is stored as kotlinx.io.Path.toString
     */
    var storageUri: String = "",

    var storageSize: Long = 0,
) {
    companion object {

        const val CACHE_FLAG_STATIC = 8


    }
}