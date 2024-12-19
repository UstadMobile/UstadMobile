package com.ustadmobile.libcache.db.dao

import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.libcache.db.entities.NewCacheEntry

@DoorDao
expect abstract class NewCacheEntryDao {

    @Query("""
        SELECT NewCacheEntry.*
          FROM NewCacheEntry
    """)
    abstract fun findAllNewEntries(): List<NewCacheEntry>

    @Query("""DELETE FROM NewCacheEntry""")
    abstract fun clearAll()

}