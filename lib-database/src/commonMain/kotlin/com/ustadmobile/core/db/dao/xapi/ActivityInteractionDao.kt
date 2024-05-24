package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.xapi.ActivityInteractionEntity
@DoorDao
@Repository
expect abstract class ActivityInteractionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertOrIgnoreAsync(entities: List<ActivityInteractionEntity>)

    @Query("""
        SELECT DISTINCT ActivityInteractionEntity.aieActivityUid
          FROM ActivityInteractionEntity
         WHERE ActivityInteractionEntity.aieActivityUid IN (:activityUids)
    """)
    abstract suspend fun findActivityUidsWithInteractionEntitiesAsync(
        activityUids: List<Long>
    ): List<Long>

    @Query("""
        SELECT ActivityInteractionEntity.*
          FROM ActivityInteractionEntity
         WHERE ActivityInteractionEntity.aieActivityUid = :activityUid 
    """)
    abstract suspend fun findAllByActivityUidAsync(
        activityUid: Long
    ): List<ActivityInteractionEntity>



}