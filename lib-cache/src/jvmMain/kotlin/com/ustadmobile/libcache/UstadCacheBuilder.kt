package com.ustadmobile.libcache

import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.libcache.UstadCache.Companion.DEFAULT_SIZE_LIMIT
import com.ustadmobile.libcache.db.AddNewEntryTriggerCallback
import com.ustadmobile.libcache.db.MIGRATE_11_12_CLIENT
import com.ustadmobile.libcache.db.MIGRATE_11_12_SERVER
import com.ustadmobile.libcache.db.MIGRATE_8_9
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.addCacheDbMigrations
import com.ustadmobile.libcache.logging.UstadCacheLogger
import com.ustadmobile.xxhashkmp.XXStringHasher
import com.ustadmobile.xxhashkmp.commonjvmimpl.XXStringHasherCommonJvm
import kotlinx.io.files.Path

/**
 * @param dbUrl JDBC URL for the Cache Database
 * @param storagePath the path where cache data will be stored
 * @param logger logging adapter (optional)
 * @param cacheName name (used in logging)
 * @param distributedCacheEnabled if true, then add triggers for distributed caching
 */
@Suppress("MemberVisibilityCanBePrivate")
class UstadCacheBuilder(
    var dbUrl: String,
    var storagePath: Path,
    var xxStringHasher: XXStringHasher = XXStringHasherCommonJvm(),
    var logger: UstadCacheLogger? = null,
    var cacheName: String = "",
    var sizeLimit: () -> Long = { DEFAULT_SIZE_LIMIT },
    var distributedCacheEnabled: Boolean = false,
    var pathsProvider: CachePathsProvider = CachePathsProvider {
        CachePaths(
            tmpWorkPath = Path(storagePath, "tmpWork"),
            persistentPath = Path(storagePath, "persistent"),
            cachePath = Path(storagePath, "cache")
        )
    },
    var db: UstadCacheDb? = null,
){

    fun build(): UstadCache {
        return UstadCacheImpl(
            pathsProvider = pathsProvider,
            db = db ?: DatabaseBuilder.databaseBuilder(UstadCacheDb::class, dbUrl, 1L)
                .addCacheDbMigrations()
                .addMigrations(MIGRATE_8_9)
                .apply {
                    if(distributedCacheEnabled) {
                        addMigrations(MIGRATE_11_12_CLIENT)
                        addCallback(AddNewEntryTriggerCallback())
                    }else {
                        addMigrations(MIGRATE_11_12_SERVER)
                    }
                }
                .build(),
            sizeLimit = sizeLimit,
            logger = logger,
            cacheName = cacheName,
            xxStringHasher = xxStringHasher,
        )
    }
}