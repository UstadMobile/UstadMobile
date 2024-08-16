package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.xapi.ActivityExtensionEntity

@DoorDao
@Repository
expect abstract class ActivityExtensionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertListAsync(list: List<ActivityExtensionEntity>)

    @Query("""
        SELECT ActivityExtensionEntity.*
          FROM ActivityExtensionEntity
         WHERE ActivityExtensionEntity.aeeActivityUid = :activityUid 
    """)
    abstract suspend fun findAllByActivityUid(activityUid: Long): List<ActivityExtensionEntity>


}