package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.StateContentEntity

@Dao
@Repository
abstract class StateContentDao : BaseDao<StateContentEntity> {

    @Query("SELECT * FROM StateContentEntity WHERE stateContentStateUid = :id AND isIsactive")
    abstract fun findAllStateContentWithStateUid(id: Long): List<StateContentEntity>

    @Query("SELECT * FROM StateContentEntity WHERE stateContentStateUid = :stateUid AND stateContentKey = :key AND isIsactive")
    abstract fun findStateContentByKeyAndStateUid(key: String, stateUid: Long): StateContentEntity?

    @Query("""UPDATE StateContentEntity SET isIsactive = :isActive,  
            stateContentLastChangedBy = COALESCE((SELECT nodeClientId FROM SyncNode LIMIT 1), 0) WHERE stateContentUid = :stateUid""")
    abstract fun setInActiveStateContentByKeyAndUid(isActive: Boolean, stateUid: Long)


}
