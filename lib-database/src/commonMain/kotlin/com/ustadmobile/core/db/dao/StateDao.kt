package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.StateEntity

@DoorDao
@Repository
expect abstract class StateDao : BaseDao<StateEntity> {

    @Query("SELECT * FROM StateEntity WHERE stateId = :id AND agentUid = :agentUid AND activityId = :activityId AND registration = :registration AND isIsactive LIMIT 1")
    abstract fun findByStateId(id: String?, agentUid: Long, activityId: String?, registration: String?): StateEntity?

    @Query("SELECT * FROM StateEntity WHERE agentUid = :agentUid AND activityId = :activityId " + "AND registration = :registration AND isIsactive AND timestamp > :since")
    @SqliteOnly
    abstract fun findStateIdByAgentAndActivity(agentUid: Long, activityId: String, registration: String, since: String): List<StateEntity>

    @Query("""
        UPDATE StateEntity 
           SET isIsactive = :isActive,
               stateLct = :updateTime
         WHERE agentUid = :agentUid AND activityId = :activityId 
           AND registration = :registration AND isIsactive
    """)
    abstract fun updateStateToInActive(
        agentUid: Long,
        activityId: String,
        registration: String,
        isActive: Boolean,
        updateTime: Long,
    )

    @Query("""
        UPDATE StateEntity 
          SET isIsactive = :isActive, 
              stateLct = :updateTime
        WHERE stateId = :stateId AND agentUid = :agentUid 
          AND activityId = :activityId AND registration = :registration 
          AND isIsactive""")
    abstract fun setStateInActive(
        stateId: String,
        agentUid: Long,
        activityId: String,
        registration: String,
        isActive: Boolean,
        updateTime: Long,
    )
}
