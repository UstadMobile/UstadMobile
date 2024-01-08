package com.ustadmobile.libcache

import com.ustadmobile.door.ext.withDoorTransaction
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.CacheEntry
import com.ustadmobile.libcache.logging.UstadCacheLogger
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path

class UstadCacheTrimmer(
    private val db: UstadCacheDb,
    private val fileSystem: FileSystem,
    private val logger: UstadCacheLogger? = null,
    private val sizeLimit: () -> Long,
) {

    /**
     *
     */
    fun trim() {
        val currentLimit = sizeLimit()
        if(currentLimit <= 0)
            throw IllegalArgumentException("Size limit must be greater than 0")

        logger?.d(UstadCacheImpl.LOG_TAG, "Trim cache run: max (evictable) size = $currentLimit bytes")
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

                db.cacheEntryDao.delete(entriesToEvict)
                pathsToDelete += entriesToEvict.map { it.storageUri }
            }
        }

        pathsToDelete.forEach { pathToDelete ->
            val path = Path(pathToDelete)
            fileSystem.takeIf { it.exists(path) }?.delete(path)
        }
    }

}