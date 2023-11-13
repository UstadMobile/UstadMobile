package com.ustadmobile.core.db.dao

import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.StateContentEntity

@DoorDao
@Repository
expect abstract class StateContentDao : BaseDao<StateContentEntity> {

    @Query("SELECT * FROM StateContentEntity WHERE stateContentStateUid = :id AND isIsactive")
    abstract fun findAllStateContentWithStateUid(id: Long): List<StateContentEntity>

    @Query("SELECT * FROM StateContentEntity WHERE stateContentStateUid = :stateUid AND stateContentKey = :key AND isIsactive")
    abstract fun findStateContentByKeyAndStateUid(key: String, stateUid: Long): StateContentEntity?

    @Query("""
        UPDATE StateContentEntity 
           SET isIsactive = :isActive,  
               stateContentLct = :updateTime
         WHERE stateContentUid = :stateUid
    """)
    abstract fun setInActiveStateContentByKeyAndUid(
        isActive: Boolean,
        stateUid: Long,
        updateTime: Long,
    )


}
