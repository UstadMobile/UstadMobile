package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.StateEntity

@Dao
@Repository
abstract class StateDao : BaseDao<StateEntity> {

    @Query("SELECT * FROM StateEntity WHERE stateId = :id AND agentUid = :agentUid AND activityId = :activityId AND registration = :registration AND isIsactive LIMIT 1")
    abstract fun findByStateId(id: String?, agentUid: Long, activityId: String?, registration: String?): StateEntity?

    @Query("SELECT * FROM StateEntity WHERE agentUid = :agentUid AND activityId = :activityId " + "AND registration = :registration AND isIsactive AND timestamp > :since")
    abstract fun findStateIdByAgentAndActivity(agentUid: Long, activityId: String, registration: String, since: String): List<StateEntity>

    @Query("""UPDATE StateEntity SET isIsactive = :isActive,
            stateLastChangedBy = COALESCE((SELECT nodeClientId FROM SyncNode LIMIT 1), 0) 
            WHERE agentUid = :agentUid AND activityId = :activityId 
            AND registration = :registration AND isIsactive""")
    abstract fun updateStateToInActive(agentUid: Long, activityId: String, registration: String, isActive: Boolean)

    @Query("""UPDATE StateEntity SET isIsactive = :isActive, 
            stateLastChangedBy = COALESCE((SELECT nodeClientId FROM SyncNode LIMIT 1), 0) 
            WHERE stateId = :stateId AND agentUid = :agentUid 
            AND activityId = :activityId AND registration = :registration 
            AND isIsactive""")
    abstract fun setStateInActive(stateId: String, agentUid: Long, activityId: String, registration: String, isActive: Boolean)
}
