package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.PostgresQuery
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.xapi.ActivityLangMapEntry

@DoorDao
@Repository
expect abstract class ActivityLangMapEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertList(entities: List<ActivityLangMapEntry>)

    /**
     * Upsert the lang map entity for an interaction entity if the related interaction entity exists
     * The interaction entity might not exists if the Activity is already defined.
     */
    @Query("""
        INSERT OR REPLACE ${ActivityLangMapEntryDaoCommon.INTO_LANG_MAP_WHERE_INTERACTION_ENTITY_EXISTS}      
    """)
    @PostgresQuery("""
        INSERT ${ActivityLangMapEntryDaoCommon.INTO_LANG_MAP_WHERE_INTERACTION_ENTITY_EXISTS}
        ON CONFLICT(almeActivityUid, almeHash) DO UPDATE
        SET almeValue = EXCLUDED.almeValue,
            almeLastMod = EXCLUDED.almeLastMod
    """)
    abstract suspend fun upsertIfInteractionEntityExists(
        almeActivityUid: Long,
        almeHash: Long,
        almeLangCode: String?,
        almeValue: String?,
        almeAieHash: Long,
        almeLastMod: Long,
    )

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