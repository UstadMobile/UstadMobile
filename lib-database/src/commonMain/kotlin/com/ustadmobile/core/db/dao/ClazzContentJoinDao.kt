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
                           ccjLastChangedBy = (SELECT nodeClientId FROM SyncNode LIMIT 1) 
                     WHERE ccjContentEntryUid IN (:selectedItem)""")
    abstract suspend fun toggleVisibilityClazzContent(toggleVisibility: Boolean, selectedItem: List<Long>)

}