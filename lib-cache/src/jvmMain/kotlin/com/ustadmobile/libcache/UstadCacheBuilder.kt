package com.ustadmobile.libcache

import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import kotlinx.io.files.Path

class UstadCacheBuilder(
    var dbUrl: String,
    var storagePath: Path,
){

    fun build(): UstadCache {
        return UstadCacheImpl(
            storagePath = storagePath,
            db = DatabaseBuilder.databaseBuilder(UstadCacheDb::class, dbUrl, 1L).build(),
            mimeTypeHelper = FileMimeTypeHelperImpl(),
        )
    }
}