package com.ustadmobile.libcache

import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.libcache.db.MIGRATE_1_2
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.logging.UstadCacheLogger
import kotlinx.io.files.Path

class UstadCacheBuilder(
    var dbUrl: String,
    var storagePath: Path,
    var logger: UstadCacheLogger? = null,
){

    fun build(): UstadCache {
        return UstadCacheImpl(
            storagePath = storagePath,
            db = DatabaseBuilder.databaseBuilder(UstadCacheDb::class, dbUrl, 1L)
                .addMigrations(MIGRATE_1_2)
                .build(),
            mimeTypeHelper = FileMimeTypeHelperImpl(),
            logger = logger,
        )
    }
}