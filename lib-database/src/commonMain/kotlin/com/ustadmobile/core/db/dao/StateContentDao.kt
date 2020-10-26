package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.StateContentEntity

@Dao
@UmRepository
abstract class StateContentDao : BaseDao<StateContentEntity> {

    @Query("SELECT * FROM StateContentEntity WHERE stateContentStateUid = :id AND isIsactive")
    abstract fun findAllStateContentWithStateUid(id: Long): List<StateContentEntity>

    @Query("SELECT * FROM StateContentEntity WHERE stateContentStateUid = :stateUid AND stateContentKey = :key AND isIsactive")
    abstract fun findStateContentByKeyAndStateUid(key: String, stateUid: Long): StateContentEntity?

    @Query("""UPDATE StateContentEntity SET isIsactive = :isActive,  
            stateContentLastChangedBy = (SELECT nodeClientId FROM SyncNode LIMIT 1) WHERE stateContentUid = :stateUid""")
    abstract fun setInActiveStateContentByKeyAndUid(isActive: Boolean, stateUid: Long)


}
