package com.ustadmobile.libcache.db.dao

import androidx.room.Delete
import androidx.room.Insert
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.libcache.db.entities.RetentionLock

@DoorDao
expect abstract class RetentionLockDao {

    @Insert
    abstract fun insert(retentionLock: RetentionLock): Long

    @Delete
    abstract fun delete(retentionLocks: List<RetentionLock>)


}