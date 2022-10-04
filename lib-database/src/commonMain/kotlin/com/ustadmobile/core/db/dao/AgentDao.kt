package com.ustadmobile.core.db.dao

import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.AgentEntity
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Person.Companion.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1
import com.ustadmobile.lib.db.entities.Person.Companion.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
import com.ustadmobile.lib.db.entities.Role

@DoorDao
@Repository
expect abstract class AgentDao :BaseDao<AgentEntity> {

    @Query("""
     REPLACE INTO AgentEntityReplicate(aePk, aeDestination)
      SELECT DISTINCT AgentEntity.agentUid AS aeUid,
             :newNodeId AS aeDestination
        FROM UserSession
        JOIN PersonGroupMember 
               ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
               ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1} 
                        ${Role.PERMISSION_PERSON_SELECT}
                        ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
        JOIN AgentEntity 
             ON AgentEntity.agentPersonUid = Person.personUid
       WHERE UserSession.usClientNodeId = :newNodeId
         --notpsql 
         AND AgentEntity.agentLct != COALESCE(
             (SELECT aeVersionId
                FROM AgentEntityReplicate
               WHERE aePk = AgentEntity.agentUid
                 AND aeDestination = UserSession.usClientNodeId), 0) 
         --endnotpsql        
      /*psql ON CONFLICT(aePk, aeDestination) DO UPDATE
             SET aePending = (SELECT AgentEntity.agentLct
                                FROM AgentEntity
                               WHERE AgentEntity.agentUid = EXCLUDED.aePk ) 
                                     != AgentEntityReplicate.aePk
      */       
     """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([AgentEntity::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
     REPLACE INTO AgentEntityReplicate(aePk, aeDestination)
      SELECT DISTINCT AgentEntity.agentUid AS aeUid,
             UserSession.usClientNodeId AS aeDestination
        FROM ChangeLog
             JOIN AgentEntity
                 ON ChangeLog.chTableId = 68
                    AND ChangeLog.chEntityPk = AgentEntity.agentUid
             JOIN Person 
                       ON Person.personUid = AgentEntity.agentPersonUid
                  $JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1
                        ${Role.PERMISSION_PERSON_SELECT}
                        $JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2       
       WHERE UserSession.usClientNodeId != (
             SELECT nodeClientId 
               FROM SyncNode
              LIMIT 1)
         --notpsql 
         AND AgentEntity.agentLct != COALESCE(
             (SELECT aeVersionId
                FROM AgentEntityReplicate
               WHERE aePk = AgentEntity.agentUid
                 AND aeDestination = UserSession.usClientNodeId), 0) 
         --endnotpsql 
      /*psql ON CONFLICT(aePk, aeDestination) DO UPDATE
             SET aePending = (SELECT AgentEntity.agentLct
                                FROM AgentEntity
                               WHERE AgentEntity.agentUid = EXCLUDED.aePk ) 
                                     != AgentEntityReplicate.aePk
      */    
    """)
    @ReplicationRunOnChange([AgentEntity::class])
    @ReplicationCheckPendingNotificationsFor([AgentEntity::class])
    abstract suspend fun replicateOnChange()


    @Query("SELECT * FROM AgentEntity WHERE agentOpenId = :openId OR agentMbox = :mbox " +
            "OR agentMbox_sha1sum = :sha1 OR (agentAccountName = :account AND agentHomePage = :homepage)")
    abstract fun getAgentByAnyId(openId: String? = "", mbox: String? = "", account: String? = "", homepage: String? = "", sha1: String? = ""): AgentEntity?


    @Query("""
        SELECT *
          FROM AgentEntity
         WHERE agentAccountName = :username 
           AND agentHomePage = :endpoint
    """)
    abstract suspend fun getAgentFromPersonUsername(endpoint: String, username: String): AgentEntity?

}
