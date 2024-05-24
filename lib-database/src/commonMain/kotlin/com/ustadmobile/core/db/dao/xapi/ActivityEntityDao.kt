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
           SET actMoreInfo = :actMoreInfo,
               actLct = :actLct
        WHERE actUid = :activityUid
          AND actMoreInfo != :actMoreInfo      
    """)
    abstract suspend fun updateIfMoreInfoChanged(
        activityUid: Long,
        actMoreInfo: String?,
        actLct: Long,
    )

    @Query("""
        UPDATE ActivityEntity
           SET actType = :actType,
               actMoreInfo = :actMoreInfo,
               actInteractionType = :actInteractionType,
               actCorrectResponsePatterns = :actCorrectResponsePatterns
         WHERE actUid = :actUid
           AND (SELECT ActivityEntityInternal.actType 
                  FROM ActivityEntity ActivityEntityInternal 
                 WHERE ActivityEntityInternal.actUid = :actUid) IS NULL
           AND (SELECT ActivityEntityInternal.actInteractionType 
                  FROM ActivityEntity ActivityEntityInternal 
                 WHERE ActivityEntityInternal.actUid = :actUid) = ${ActivityEntity.TYPE_UNSET}
           AND (SELECT ActivityEntityInternal.actCorrectResponsePatterns 
                  FROM ActivityEntity ActivityEntityInternal 
                 WHERE ActivityEntityInternal.actUid = :actUid) IS NULL      
    """)
    abstract suspend fun updateIfNotYetDefined(
        actUid: Long,
        actType: String?,
        actMoreInfo: String?,
        actInteractionType: Int,
        actCorrectResponsePatterns: String?
    )

    @Query("""
        SELECT ActivityEntity.*
          FROM ActivityEntity
         WHERE ActivityEntity.actUid = :activityUid 
    """)
    abstract suspend fun findByUidAsync(activityUid: Long): ActivityEntity?


}