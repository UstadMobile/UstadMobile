package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.SyncNode

@DoorDao
expect abstract class SyncNodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replace(syncNode: SyncNode)

    @Query("""
        SELECT COALESCE(
               (SELECT nodeClientId 
                  FROM SyncNode 
                 LIMIT 1), 0)
    """)
    abstract suspend fun getLocalNodeClientId(): Long

}