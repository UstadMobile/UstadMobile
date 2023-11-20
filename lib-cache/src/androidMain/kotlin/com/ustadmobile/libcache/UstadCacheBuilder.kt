package com.ustadmobile.libcache

import android.content.Context
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

class UstadCacheBuilder(
    var appContext: Context,
    var storagePath: Path,
    var dbName: String = "UstadCache",
) {

    fun build(): UstadCache {
        return UstadCacheImpl(
            fileSystem = SystemFileSystem,
            storagePath = storagePath,
            db = DatabaseBuilder.databaseBuilder(
                context = appContext,
                dbClass = UstadCacheDb::class,
                dbName = dbName,
                nodeId = 1L
            ).build(),
            mimeTypeHelper = FileMimeTypeHelperImpl()
        )
    }

}