package com.ustadmobile.libcache.db.entities

import java.util.concurrent.locks.ReentrantLock

/**
 * Data structure used to hold a CacheEntry and related locks.
 */
data class CacheEntryAndLocks(
    val urlKey: String,
    val entry: CacheEntry?,
    val locks: List<RetentionLock>,
    val moveLock: ReentrantLock = ReentrantLock(false),
)

