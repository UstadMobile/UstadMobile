package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*

@Repository
@DoorDao
expect abstract class PersonGroupDao : BaseDao<PersonGroup> {

    @Query("SELECT * FROM PersonGroup WHERE groupUid = :uid")
    abstract fun findByUid(uid: Long): PersonGroup?

    @Query("SELECT * FROM PersonGroup WHERE groupUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : PersonGroup?

    @Query("SELECT * FROM PersonGroup WHERE groupUid = :uid")
    abstract fun findByUidLive(uid: Long): Flow<PersonGroup?>


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
