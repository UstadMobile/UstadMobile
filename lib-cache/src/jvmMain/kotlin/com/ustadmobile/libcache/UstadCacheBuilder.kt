package com.ustadmobile.libcache

import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.libcache.db.MIGRATE_1_2
import com.ustadmobile.libcache.db.MIGRATE_2_3
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
class UstadCacheBuilder(
    var dbUrl: String,
    var storagePath: Path,
    var logger: UstadCacheLogger? = null,
    var cacheName: String = "",
){

    fun build(): UstadCache {
        return UstadCacheImpl(
            storagePath = storagePath,
            db = DatabaseBuilder.databaseBuilder(UstadCacheDb::class, dbUrl, 1L)
                .addMigrations(MIGRATE_1_2)
                .addMigrations(MIGRATE_2_3)
                .build(),
            mimeTypeHelper = FileMimeTypeHelperImpl(),
            logger = logger,
            cacheName = cacheName,
        )
    }
}