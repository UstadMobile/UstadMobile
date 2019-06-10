package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.StateEntity

@Dao
@UmRepository
abstract class StateDao : BaseDao<StateEntity> {

    @Query("SELECT * FROM StateEntity WHERE stateId = :id AND agentUid = :agentUid AND activityId = :activityId " + "AND registration = :registration AND isactive LIMIT 1")
    abstract fun findByStateId(id: String?, agentUid: Long?, activityId: String?, registration: String?): StateEntity?

    @Query("SELECT * FROM StateEntity WHERE agentUid = :agentUid AND activityId = :activityId " + "AND registration = :registration AND isactive AND timestamp > :since")
    abstract fun findStateIdByAgentAndActivity(agentUid: Long, activityId: String, registration: String, since: String): List<StateEntity>

    @Query("UPDATE StateEntity SET isactive = :isActive WHERE agentUid = :agentUid AND activityId = :activityId " + "AND registration = :registration AND isactive")
    abstract fun updateStateToInActive(agentUid: Long, activityId: String, registration: String, isActive: Boolean)

    @Query("UPDATE StateEntity SET isactive = :isActive WHERE stateId = :stateId AND agentUid = :agentUid AND activityId = :activityId " + "AND registration = :registration AND isactive")
    abstract fun setStateInActive(stateId: String, agentUid: Long, activityId: String, registration: String, isActive: Boolean)
}
