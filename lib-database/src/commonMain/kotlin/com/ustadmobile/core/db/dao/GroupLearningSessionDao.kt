package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.GroupLearningSession
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Person.Companion.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1
import com.ustadmobile.lib.db.entities.Person.Companion.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UserSession

@Repository
@DoorDao
expect abstract class GroupLearningSessionDao : BaseDao<GroupLearningSession> {

    @Query("""
     REPLACE INTO GroupLearningSessionReplicate(glsPk, glsDestination)
      SELECT DISTINCT GroupLearningSession.groupLearningSessionUid AS glsPk,
             :newNodeId AS glsDestination
        FROM UserSession
             JOIN PersonGroupMember
                  ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             $JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1
                  ${Role.PERMISSION_PERSON_SELECT}
                  $JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2
             JOIN LearnerGroupMember
                  ON LearnerGroupMember.learnerGroupMemberPersonUid = Person.personUid
             JOIN GroupLearningSession
                  ON GroupLearningSession.groupLearningSessionLearnerGroupUid = LearnerGroupMember.learnerGroupMemberLgUid
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND GroupLearningSession.groupLearningSessionLct != COALESCE(
             (SELECT glsVersionId
                FROM GroupLearningSessionReplicate
               WHERE glsPk = GroupLearningSession.groupLearningSessionUid
                 AND glsDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(glsPk, glsDestination) DO UPDATE
             SET glsPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([GroupLearningSession::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO GroupLearningSessionReplicate(glsPk, glsDestination)
  SELECT DISTINCT GroupLearningSession.groupLearningSessionUid AS glsUid,
         UserSession.usClientNodeId AS glsDestination
    FROM ChangeLog
         JOIN GroupLearningSession
              ON ChangeLog.chTableId = ${GroupLearningSession.TABLE_ID}
                 AND ChangeLog.chEntityPk = GroupLearningSession.groupLearningSessionUid
         JOIN LearnerGroupMember
              ON LearnerGroupMember.learnerGroupMemberLgUid = GroupLearningSession.groupLearningSessionLearnerGroupUid
         JOIN Person
              ON Person.personUid = LearnerGroupMember.learnerGroupMemberPersonUid
         ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_PERSON_SELECT}
              ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND GroupLearningSession.groupLearningSessionLct != COALESCE(
         (SELECT glsVersionId
            FROM GroupLearningSessionReplicate
           WHERE glsPk = GroupLearningSession.groupLearningSessionUid
             AND glsDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(glsPk, glsDestination) DO UPDATE
     SET glsPending = true
  */               
    """)
    @ReplicationRunOnChange([GroupLearningSession::class])
    @ReplicationCheckPendingNotificationsFor([GroupLearningSession::class])
    abstract suspend fun replicateOnChange()

}
