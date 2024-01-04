package com.ustadmobile.libcache.db

import com.ustadmobile.door.annotation.DoorDatabase
import com.ustadmobile.door.room.RoomDatabase
import com.ustadmobile.libcache.db.dao.CacheEntryDao
import com.ustadmobile.libcache.db.dao.ResponseBodyDao
import com.ustadmobile.libcache.db.dao.RequestedEntryDao
import com.ustadmobile.libcache.db.dao.RetentionLockDao
import com.ustadmobile.libcache.db.entities.CacheEntry
import com.ustadmobile.libcache.db.entities.RequestedEntry
import com.ustadmobile.libcache.db.entities.ResponseBody
import com.ustadmobile.libcache.db.entities.RetentionLock

/**
 * CacheEntry
 *  url, headers, etc.
 *
 * ResponseData
 *  sha256 storageUri
 *
 * RetentionLock
 *   LockId
 *   EntryId
 *
 */
@DoorDatabase(
    version = 3,
    entities = arrayOf(
        CacheEntry::class,
        ResponseBody::class,
        RequestedEntry::class,
        RetentionLock::class,
    ),
)
expect abstract class UstadCacheDb : RoomDatabase {

    abstract val cacheEntryDao: CacheEntryDao

    abstract val responseBodyDao: ResponseBodyDao

    abstract val requestedEntryDao: RequestedEntryDao

    abstract val retentionLockDao: RetentionLockDao

}