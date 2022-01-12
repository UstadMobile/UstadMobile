package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ClazzContentJoin

@Repository
@Dao
abstract class ClazzContentJoinDao: BaseDao<ClazzContentJoin> {

    @Query("""UPDATE ClazzContentJoin 
                       SET ccjActive = :toggleVisibility, 
                           ccjLastChangedBy =  COALESCE((SELECT nodeClientId FROM SyncNode LIMIT 1), 0) 
                     WHERE ccjContentEntryUid IN (:selectedItem)""")
    abstract suspend fun toggleVisibilityClazzContent(toggleVisibility: Boolean, selectedItem: List<Long>)

    @Query("""
        SELECT ccjContentEntryUid 
          FROM ClazzContentJoin
         WHERE ccjClazzUid = :clazzUid
           AND ccjActive
    """)
    abstract suspend fun listOfEntriesInClazz(clazzUid: Long): List<Long>

}