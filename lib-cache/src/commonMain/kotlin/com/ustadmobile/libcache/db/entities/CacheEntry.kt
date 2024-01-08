package com.ustadmobile.libcache.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a cache entry.
 *
 * @param key The url key is the md5 of the URL (base64 encoded). This creates a unique key based on
 *        the URL, and improves performance when searching/indexing because it is shorter than the
 *        url itself. URLs by nature often have matching prefixes (which slows down searching through
 *        them as an equality check has to go further through a non-matching string before it can
 *        return false).
 * @param cacheFlags flags from the cache-control header.
 */
@Entity(
    indices = arrayOf(
        Index("lastAccessed", name = "idx_lastAccessed")
    )
)
data class CacheEntry(
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
    var integrity: String? = null,

    var responseHeaders: String = "",

    /**
     * The path where the body of the request is stored as kotlinx.io.Path.toString
     */
    var storageUri: String = "",

    var storageSize: Long = 0,
)
