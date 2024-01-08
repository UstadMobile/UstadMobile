package com.ustadmobile.libcache

import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.libcache.UstadCache.Companion.DEFAULT_SIZE_LIMIT
import com.ustadmobile.libcache.db.MIGRATE_1_2
import com.ustadmobile.libcache.db.MIGRATE_2_3
import com.ustadmobile.libcache.db.MIGRATE_3_4
import com.ustadmobile.libcache.db.MIGRATE_4_5
import com.ustadmobile.libcache.db.MIGRATE_5_6
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
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
                .addMigrations(MIGRATE_1_2, MIGRATE_2_3, MIGRATE_3_4, MIGRATE_4_5,
                    MIGRATE_5_6)
                .build(),
            sizeLimit = sizeLimit,
            mimeTypeHelper = FileMimeTypeHelperImpl(),
            logger = logger,
            cacheName = cacheName,
        )
    }
}