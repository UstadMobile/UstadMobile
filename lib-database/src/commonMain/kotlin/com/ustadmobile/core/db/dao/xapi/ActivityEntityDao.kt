package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.xapi.ActivityEntity

@DoorDao
@Repository
expect abstract class ActivityEntityDao  {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertOrIgnoreAsync(entities: List<ActivityEntity>)

    @Query("""
        UPDATE ActivityEntity
           SET actType = :actType,
               actMoreInfo = :actMoreInfo,
               actInteractionType = :actInteractionType,
               actCorrectResponsePatterns = :actCorrectResponsePatterns,
               actLct = :actLct
        WHERE actUid = :activityUid
         AND (    actType != :actType
               OR actMoreInfo != :actMoreInfo
               OR actInteractionType != :actInteractionType
               OR actCorrectResponsePatterns != :actCorrectResponsePatterns)      
    """)
    abstract suspend fun updateIfChanged(
        activityUid: Long,
        actType: String?,
        actMoreInfo: String?,
        actInteractionType: Int,
        actCorrectResponsePatterns: String?,
        actLct: Long,
    )

    @Query("""
        SELECT ActivityEntity.*
          FROM ActivityEntity
         WHERE ActivityEntity.actUid = :activityUid 
    """)
    abstract suspend fun findByUidAsync(activityUid: Long): ActivityEntity?


}