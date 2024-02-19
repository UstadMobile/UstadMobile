package com.ustadmobile.libcache.db.entities

/**
 * Data structure used to hold a CacheEntry and related locks.
 */
data class CacheEntryAndLocks(
    val urlKey: String,
    val entry: CacheEntry?,
    val locks: List<RetentionLock>,
)

