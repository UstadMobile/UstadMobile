package com.ustadmobile.core.db.dao

import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.LearnerGroup
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UserSession

@Repository
@DoorDao
expect abstract class LearnerGroupDao : BaseDao<LearnerGroup> {

    @Query("""
     REPLACE INTO LearnerGroupReplicate(lgPk, lgDestination)
      SELECT DISTINCT LearnerGroup.learnerGroupUid AS lgPk,
             :newNodeId AS lgDestination
        FROM UserSession
             JOIN PersonGroupMember
                  ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
                  ${Role.PERMISSION_PERSON_SELECT}
                  ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
             JOIN LearnerGroupMember
                  ON LearnerGroupMember.learnerGroupMemberPersonUid = Person.personUid
             JOIN LearnerGroup
                  ON LearnerGroup.learnerGroupUid = LearnerGroupMember.learnerGroupMemberLgUid
            WHERE UserSession.usClientNodeId = :newNodeId
              AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
              AND LearnerGroup.learnerGroupLct != COALESCE(
                  (SELECT lgVersionId
                     FROM LearnerGroupReplicate
                    WHERE lgPk = LearnerGroup.learnerGroupUid
                      AND lgDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(lgPk, lgDestination) DO UPDATE
             SET lgPending = true
      */       
 """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([LearnerGroup::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO LearnerGroupReplicate(lgPk, lgDestination)
  SELECT DISTINCT LearnerGroup.learnerGroupUid AS lgUid,
         UserSession.usClientNodeId AS lgDestination
    FROM ChangeLog
         JOIN LearnerGroup
              ON ChangeLog.chTableId = ${LearnerGroup.TABLE_ID}
                 AND ChangeLog.chEntityPk = LearnerGroup.learnerGroupUid
         JOIN LearnerGroupMember
              ON LearnerGroupMember.learnerGroupMemberLgUid = LearnerGroup.learnerGroupUid
         JOIN Person
              ON Person.personUid = LearnerGroupMember.learnerGroupMemberPersonUid
         ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_PERSON_SELECT}
              ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
 /*psql ON CONFLICT(lgPk, lgDestination) DO UPDATE
     SET lgPending = true
  */               
    """)
    @ReplicationRunOnChange([LearnerGroup::class])
    @ReplicationCheckPendingNotificationsFor([LearnerGroup::class])
    abstract suspend fun replicateOnChange()

    @Query("""SELECT LearnerGroup.* FROM LearnerGroup 
            LEFT JOIN GroupLearningSession ON 
            GroupLearningSession.groupLearningSessionLearnerGroupUid = LearnerGroup.learnerGroupUid 
            WHERE GroupLearningSession.groupLearningSessionContentUid = :contentEntryUid""")
    abstract fun findGroupsForEntryAsync(contentEntryUid: Long): DataSourceFactory<Int, LearnerGroup>

    @Query("""SELECT LearnerGroup.* FROM LearnerGroup 
            LEFT JOIN GroupLearningSession ON 
            GroupLearningSession.groupLearningSessionLearnerGroupUid = LearnerGroup.learnerGroupUid 
            WHERE GroupLearningSession.groupLearningSessionContentUid = :contentEntryUid""")
    abstract fun findGroupListForEntry(contentEntryUid: Long): List<LearnerGroup>

}