package com.ustadmobile.lib.db.entities.xapi

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository

@DoorDao
@Repository
expect abstract class ActivityLangMapEntryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertOrIgnoreList(entities: List<ActivityLangMapEntry>)

    @Query("""
        UPDATE ActivityLangMapEntry
           SET almeValue = :almeValue,
               almeLastMod = :almeLastMod
         WHERE almeActivityUid = :almeActivityUid
           AND almeHash = :almeHash
           AND almeValue != :almeValue       
    """)
    abstract suspend fun updateIfChanged(
        almeActivityUid: Long,
        almeHash: Long,
        almeValue: String?,
        almeLastMod: Long,
    )

    @Query("""
        SELECT ActivityLangMapEntry.*
          FROM ActivityLangMapEntry
         WHERE ActivityLangMapEntry.almeActivityUid = :activityUid
    """)
    abstract suspend fun findAllByActivityUid(activityUid: Long): List<ActivityLangMapEntry>


}