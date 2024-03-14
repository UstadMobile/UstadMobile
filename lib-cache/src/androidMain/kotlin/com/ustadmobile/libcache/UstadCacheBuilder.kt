package com.ustadmobile.libcache

import android.content.Context
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.libcache.db.MIGRATE_8_9
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.addCacheDbMigrations
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
    var cachePathsProvider: CachePathsProvider = CachePathsProvider {
        CachePaths(
            tmpWorkPath = Path(storagePath, DEFAULT_SUBPATH_WORK),
            persistentPath = Path(storagePath, DEFAULT_SUBPATH_PERSISTENT),
            cachePath = Path(appContext.cacheDir.absolutePath, DEFAULT_SUBPATH_CACHE),
        )
    }
) {

    fun build(): UstadCache {
        return UstadCacheImpl(
            fileSystem = SystemFileSystem,
            pathsProvider = cachePathsProvider,
            logger =  logger,
            sizeLimit = sizeLimit,
            db = DatabaseBuilder.databaseBuilder(
                context = appContext,
                dbClass = UstadCacheDb::class,
                dbName = dbName,
                nodeId = 1L
            )
            .addCacheDbMigrations()
            .addMigrations(MIGRATE_8_9)
            .build()
        )
    }

    companion object {

        const val DEFAULT_SUBPATH_WORK = "tmpwork"

        const val DEFAULT_SUBPATH_PERSISTENT = "persistent"

        const val DEFAULT_SUBPATH_CACHE = "ustad-cache"

    }

}