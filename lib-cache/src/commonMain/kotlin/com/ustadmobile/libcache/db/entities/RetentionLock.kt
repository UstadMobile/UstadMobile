package com.ustadmobile.libcache.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * The presence of a retention lock for an entry in the cache prevents it from being evicted. One
 * entry can have zero to many locks.
 *
 * This ensures that a particular item will be available in the cache on demand (e.g. if a user
 * specifically requests a given item, the cache entry will be kept indefinitely, even when it
 * otherwise be evicted based on last access time).
 */
@Entity(
    indices = arrayOf(
        Index("lockKey", name = "idx_lockKey")
    )
)
data class RetentionLock(
    @PrimaryKey(autoGenerate = true)
    var lockId: Int = 0,

    /**
     * The key as per CacheEntry.key
     */
    var lockKey: String = "",

    /**
     * A remark that can be added at the time of creating the lock e.g. a note on why the entry
     * is to be retained
     */
    var lockRemark: String = "",
)
