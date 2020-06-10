package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.door.SyncNode

@Dao
abstract class SyncNodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replace(syncNode: SyncNode)

}