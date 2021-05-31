package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.lib.db.entities.ScopedGrantAndName

@Dao
@Repository
abstract class ScopedGrantDao {

    @Insert
    abstract suspend fun insertAsync(scopedGrant: ScopedGrant): Long

    @Insert
    abstract suspend fun insertListAsync(scopedGrantList: List<ScopedGrant>)

    @Update
    abstract suspend fun updateAsync(scopedGrant: ScopedGrant)

    @Update
    abstract suspend fun updateListAsync(scopedGrantList: List<ScopedGrant>)

    @Query("""
        SELECT ScopedGrant.*,
               CASE
               WHEN Person.firstNames IS NOT NULL THEN Person.firstNames
               ELSE PersonGroup.groupName 
               END AS name
          FROM ScopedGrant
               JOIN PersonGroup 
                    ON ScopedGrant.sgGroupUid = PersonGroup.groupUid
               LEFT JOIN Person
                         ON Person.personGroupUid = PersonGroup.groupUid
         WHERE ScopedGrant.sgTableId = :tableId
               AND ScopedGrant.sgEntityUid = :entityUid 
    """)
    abstract suspend fun findByTableIdAndEntityUid(tableId: Int, entityUid: Long): List<ScopedGrantAndName>


}