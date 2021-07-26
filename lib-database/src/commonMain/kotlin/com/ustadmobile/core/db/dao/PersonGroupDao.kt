package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.PersonGroupWithMemberCount
import com.ustadmobile.lib.db.entities.PersonGroup

@Repository
@Dao
abstract class PersonGroupDao : BaseDao<PersonGroup> {

    @Query("SELECT * FROM PersonGroup WHERE groupUid = :uid")
    abstract fun findByUid(uid: Long): PersonGroup?

    @Query("SELECT * FROM PersonGroup WHERE groupUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : PersonGroup?

    @Query("SELECT * FROM PersonGroup WHERE groupUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<PersonGroup?>

    @Query("""
        SELECT PersonGroup.*, 
        ( SELECT COUNT(*) FROM PersonGroupMember 
            WHERE groupMemberGroupUid = PersonGroup.groupUid 
            AND CAST(groupMemberActive AS INTEGER) = 1 
        ) as memberCount
        FROM PersonGroup WHERE CAST(groupActive AS INTEGER) = 1 
    """)
    abstract fun getAllGroupsLive():
            DataSource.Factory<Int, PersonGroupWithMemberCount>

    @Update
    abstract suspend fun updateAsync(entity: PersonGroup) : Int

    @Query("""
        Select CASE
               WHEN Person.firstNames IS NOT NULL THEN Person.firstNames
               ELSE PersonGroup.groupName 
               END AS name
          FROM PersonGroup
               LEFT JOIN Person
                         ON Person.personGroupUid = PersonGroup.groupUid
         WHERE PersonGroup.groupUid = :groupUid
         LIMIT 1
    """)
    abstract suspend fun findNameByGroupUid(groupUid: Long): String?

}
