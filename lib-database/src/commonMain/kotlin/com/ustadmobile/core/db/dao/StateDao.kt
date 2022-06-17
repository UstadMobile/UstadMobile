package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.StateEntity
import com.ustadmobile.lib.db.entities.UserSession

@Dao
@Repository
abstract class StateDao : BaseDao<StateEntity> {

    @Query("""
     REPLACE INTO StateEntityReplicate(sePk, seDestination)
      SELECT DISTINCT StateEntity.stateUid AS sePk,
             :newNodeId AS seDestination
        FROM StateEntity
             JOIN AgentEntity
                  ON StateEntity.agentUid = AgentEntity.agentUid
             JOIN UserSession
                  ON AgentEntity.agentPersonUid = UserSession.usPersonUid
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND StateEntity.stateLct != COALESCE(
             (SELECT seVersionId
                FROM StateEntityReplicate
               WHERE sePk = StateEntity.stateUid
                 AND seDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(sePk, seDestination) DO UPDATE
             SET sePending = true
      */       
 """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([StateEntity::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO StateEntityReplicate(sePk, seDestination)
  SELECT DISTINCT StateEntity.stateUid AS seUid,
         UserSession.usClientNodeId AS seDestination
    FROM ChangeLog
         JOIN StateEntity
              ON ChangeLog.chTableId = ${StateEntity.TABLE_ID}
                 AND ChangeLog.chEntityPk = StateEntity.stateUid
         JOIN AgentEntity
              ON StateEntity.agentUid = AgentEntity.agentUid
         JOIN UserSession
              ON AgentEntity.agentPersonUid = UserSession.usPersonUid
                 AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND StateEntity.stateLct != COALESCE(
         (SELECT seVersionId
            FROM StateEntityReplicate
           WHERE sePk = StateEntity.stateUid
             AND seDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(sePk, seDestination) DO UPDATE
     SET sePending = true
  */               
    """)
    @ReplicationRunOnChange([StateEntity::class])
    @ReplicationCheckPendingNotificationsFor([StateEntity::class])
    abstract suspend fun replicateOnChange()

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
