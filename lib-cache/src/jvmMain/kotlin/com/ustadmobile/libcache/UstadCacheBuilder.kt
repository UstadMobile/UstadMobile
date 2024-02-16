package com.ustadmobile.libcache

import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.libcache.UstadCache.Companion.DEFAULT_SIZE_LIMIT
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.addCacheDbMigrations
import com.ustadmobile.libcache.logging.UstadCacheLogger
import kotlinx.io.files.Path

/**
 * @param dbUrl JDBC URL for the Cache Database
 * @param storagePath the path where cache data will be stored
 * @param logger logging adapter (optional)
 * @param cacheName name (used in logging)
 */
@Suppress("MemberVisibilityCanBePrivate")
class UstadCacheBuilder(
    var dbUrl: String,
    var storagePath: Path,
    var logger: UstadCacheLogger? = null,
    var cacheName: String = "",
    var sizeLimit: () -> Long = { DEFAULT_SIZE_LIMIT }
){

    fun build(): UstadCache {
        return UstadCacheImpl(
            storagePath = storagePath,
            db = DatabaseBuilder.databaseBuilder(UstadCacheDb::class, dbUrl, 1L)
                .addCacheDbMigrations()
                .build(),
            sizeLimit = sizeLimit,
            logger = logger,
            cacheName = cacheName,
        )
    }
}