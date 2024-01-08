package com.ustadmobile.libcache

import android.content.Context
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.logging.UstadCacheLogger
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

@Suppress("MemberVisibilityCanBePrivate")
class UstadCacheBuilder(
    var appContext: Context,
    var storagePath: Path,
    var dbName: String = "UstadCache",
    var logger: UstadCacheLogger? = null,
    var sizeLimit: () -> Long,
) {

    fun build(): UstadCache {
        return UstadCacheImpl(
            fileSystem = SystemFileSystem,
            storagePath = storagePath,
            logger =  logger,
            sizeLimit = sizeLimit,
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