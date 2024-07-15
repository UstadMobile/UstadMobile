package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.xapi.GroupMemberActorJoin

@Repository
@DoorDao
expect abstract class GroupMemberActorJoinDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertOrIgnoreListAsync(entities: List<GroupMemberActorJoin>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertListAsync(entities: List<GroupMemberActorJoin>)

    @Query("""
        UPDATE GroupMemberActorJoin
           SET gmajLastMod = :lastModTime
         WHERE gmajGroupActorUid = :gmajGroupActorUid
           AND gmajMemberActorUid = :gmajMemberActorUid
           AND gmajLastMod != :lastModTime 
    """)
    abstract suspend fun updateLastModifiedTimeIfNeededAsync(
        gmajGroupActorUid: Long,
        gmajMemberActorUid: Long,
        lastModTime: Long
    )

}