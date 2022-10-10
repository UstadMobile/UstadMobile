package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*

@Repository
@DoorDao
expect abstract class LearnerGroupMemberDao : BaseDao<LearnerGroupMember> {

    @Query("""
     REPLACE INTO LearnerGroupMemberReplicate(lgmPk, lgmDestination)
      SELECT DISTINCT LearnerGroupMember.learnerGroupMemberUid AS lgmPk,
             :newNodeId AS lgmDestination
        FROM UserSession
             JOIN PersonGroupMember
                  ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
                  ${Role.PERMISSION_PERSON_SELECT}
                  ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
             JOIN LearnerGroupMember
                  ON LearnerGroupMember.learnerGroupMemberPersonUid = Person.personUid
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND LearnerGroupMember.learnerGroupMemberLct != COALESCE(
             (SELECT lgmVersionId
                FROM LearnerGroupMemberReplicate
               WHERE lgmPk = LearnerGroupMember.learnerGroupMemberUid
                 AND lgmDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(lgmPk, lgmDestination) DO UPDATE
             SET lgmPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([LearnerGroupMember::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO LearnerGroupMemberReplicate(lgmPk, lgmDestination)
  SELECT DISTINCT LearnerGroupMember.learnerGroupMemberUid AS lgmUid,
         UserSession.usClientNodeId AS lgmDestination
    FROM ChangeLog
         JOIN LearnerGroupMember
              ON ChangeLog.chTableId = ${LearnerGroupMember.TABLE_ID}
                 AND ChangeLog.chEntityPk = LearnerGroupMember.learnerGroupMemberUid
         JOIN Person
              ON Person.personUid = LearnerGroupMember.learnerGroupMemberPersonUid
         ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_PERSON_SELECT}
              ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId
           FROM SyncNode
          LIMIT 1)
     AND LearnerGroupMember.learnerGroupMemberLct != COALESCE(
         (SELECT lgmVersionId
            FROM LearnerGroupMemberReplicate
           WHERE lgmPk = LearnerGroupMember.learnerGroupMemberUid
             AND lgmDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(lgmPk, lgmDestination) DO UPDATE
     SET lgmPending = true
  */
    """)
    @ReplicationRunOnChange([LearnerGroupMember::class])
    @ReplicationCheckPendingNotificationsFor([LearnerGroupMember::class])
    abstract suspend fun replicateOnChange()

    @Query("""SELECT LearnerGroupMember.*, Person.* FROM LearnerGroupMember 
        LEFT JOIN Person ON LearnerGroupMember.learnerGroupMemberPersonUid = Person.personUid 
        LEFT JOIN GroupLearningSession ON 
    GroupLearningSession.groupLearningSessionLearnerGroupUid = LearnerGroupMember.learnerGroupMemberLgUid 
    WHERE GroupLearningSession.groupLearningSessionLearnerGroupUid = :learnerGroupUid 
    AND GroupLearningSession.groupLearningSessionContentUid = :contentEntryUid 
    ORDER BY learnerGroupMemberRole ASC
    """)
    abstract fun findLearnerGroupMembersByGroupIdAndEntry(learnerGroupUid: Long, contentEntryUid: Long): DataSourceFactory<Int, LearnerGroupMemberWithPerson>

    @Query("""SELECT LearnerGroupMember.*, Person.* FROM LearnerGroupMember 
        LEFT JOIN Person ON LearnerGroupMember.learnerGroupMemberPersonUid = Person.personUid 
        LEFT JOIN GroupLearningSession ON 
    GroupLearningSession.groupLearningSessionLearnerGroupUid = LearnerGroupMember.learnerGroupMemberLgUid 
    WHERE GroupLearningSession.groupLearningSessionLearnerGroupUid = :learnerGroupUid 
    AND GroupLearningSession.groupLearningSessionContentUid = :contentEntryUid 
    ORDER BY learnerGroupMemberRole ASC
    """)
    abstract suspend fun findLearnerGroupMembersByGroupIdAndEntryList(learnerGroupUid: Long, contentEntryUid: Long): List<LearnerGroupMemberWithPerson>

}