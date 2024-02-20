package com.ustadmobile.libcache

import com.ustadmobile.door.ext.withDoorTransaction
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.CacheEntry
import com.ustadmobile.libcache.logging.UstadCacheLogger
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path

class UstadCacheTrimmer(
    private val db: UstadCacheDb,
    private val fileSystem: FileSystem,
    private val logger: UstadCacheLogger? = null,
    private val sizeLimit: () -> Long,
) {

    private val logPrefix = "CacheTrimmer: "

    private val _evictedEntriesFlow = MutableSharedFlow<List<String>>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * A flow of entries that have been evicted. This can be observed by the cache to update the
     * in memory map as required.
     */
    val evictedEntriesFlow: Flow<List<String>> = _evictedEntriesFlow.asSharedFlow()

    /**
     *
     */
    fun trim() {
        val currentLimit = sizeLimit()
        if(currentLimit <= 0)
            throw IllegalArgumentException("Size limit must be greater than 0")

        logger?.d(UstadCacheImpl.LOG_TAG, "$logPrefix Trim cache run: max (evictable) size = $currentLimit bytes")
        val pathsToDelete = mutableListOf<String>()
        db.withDoorTransaction {
            var currentSize: Long
            while(db.cacheEntryDao.totalEvictableSize().also { currentSize = it } > currentLimit) {
                val deleteTarget = currentSize - currentLimit
                val evictableEntries = db.cacheEntryDao.findEvictableEntries(100)
                val entriesToEvict = mutableListOf<CacheEntry>()
                var entriesToEvictSize = 0L

                for(entry in evictableEntries) {
                    entriesToEvict += entry
                    entriesToEvictSize += entry.storageSize
                    if(entriesToEvictSize >= deleteTarget)
                        break
                }
                _evictedEntriesFlow.tryEmit(evictableEntries.map { it.key })
                db.cacheEntryDao.delete(entriesToEvict)
                logger?.v(UstadCacheImpl.LOG_TAG, "$logPrefix evicting ${entriesToEvict.map { it.url }}")
                pathsToDelete += entriesToEvict.map { it.storageUri }
            }
        }

        logger?.v(UstadCacheImpl.LOG_TAG, "$logPrefix deleting ${pathsToDelete.joinToString()}")
        pathsToDelete.forEach { pathToDelete ->
            val path = Path(pathToDelete)
            fileSystem.takeIf { it.exists(path) }?.delete(path)
        }
    }

}