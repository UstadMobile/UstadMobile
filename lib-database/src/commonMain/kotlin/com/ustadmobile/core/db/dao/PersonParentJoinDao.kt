package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.*
import com.ustadmobile.door.annotation.Repository.Companion.METHOD_DELEGATE_TO_WEB
import com.ustadmobile.lib.db.entities.*

@Dao
@Repository
abstract class PersonParentJoinDao {

    @Query("""
     REPLACE INTO PersonParentJoinReplicate(ppjPk, ppjDestination)
      SELECT DISTINCT PersonParentJoin.ppjUid AS ppjPk,
             :newNodeId AS ppjDestination
        FROM UserSession
             JOIN PersonGroupMember
                  ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
                  ${Role.PERMISSION_PERSON_SELECT}
                  ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
             JOIN PersonParentJoin
                  ON PersonParentJoin.ppjParentPersonUid = Person.personUid       
       WHERE UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND PersonParentJoin.ppjLct != COALESCE(
             (SELECT ppjVersionId
                FROM PersonParentJoinReplicate
               WHERE ppjPk = PersonParentJoin.ppjUid
                 AND ppjDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(ppjPk, ppjDestination) DO UPDATE
             SET ppjPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([PersonParentJoin::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO PersonParentJoinReplicate(ppjPk, ppjDestination)
  SELECT DISTINCT PersonParentJoin.ppjUid AS ppjUid,
         UserSession.usClientNodeId AS ppjDestination
    FROM ChangeLog
         JOIN PersonParentJoin
             ON ChangeLog.chTableId = ${PersonParentJoin.TABLE_ID}
                AND ChangeLog.chEntityPk = PersonParentJoin.ppjUid
         JOIN Person
              ON PersonParentJoin.ppjParentPersonUid = Person.personUid
         ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_PERSON_SELECT}
              ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}       
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId
           FROM SyncNode
          LIMIT 1)
     AND PersonParentJoin.ppjLct != COALESCE(
         (SELECT ppjVersionId
            FROM PersonParentJoinReplicate
           WHERE ppjPk = PersonParentJoin.ppjUid
             AND ppjDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(ppjPk, ppjDestination) DO UPDATE
     SET ppjPending = true
  */
    """)
    @ReplicationRunOnChange([PersonParentJoin::class])
    @ReplicationCheckPendingNotificationsFor([PersonParentJoin::class])
    abstract suspend fun replicateOnChange()

    @Insert
    abstract suspend fun insertAsync(entity: PersonParentJoin): Long

    @Query("""
        SELECT PersonParentJoin.*, Person.*
          FROM PersonParentJoin
     LEFT JOIN Person ON Person.personUid = PersonParentJoin.ppjMinorPersonUid    
         WHERE PersonParentJoin.ppjUid = :uid
    """)
    abstract suspend fun findByUidWithMinorAsync(uid: Long): PersonParentJoinWithMinorPerson?

    @Query("""
        SELECT PersonParentJoin.*, Person.*
          FROM PersonParentJoin
     LEFT JOIN Person ON Person.personUid = PersonParentJoin.ppjMinorPersonUid    
         WHERE PersonParentJoin.ppjUid = :uid
    """)
    @RepoHttpAccessible
    @Repository(METHOD_DELEGATE_TO_WEB)
    abstract suspend fun findByUidWithMinorAsyncFromWeb(uid: Long): PersonParentJoinWithMinorPerson?

    @Query("""
        SELECT PersonParentJoin.*
          FROM PersonParentJoin
         WHERE ppjMinorPersonUid = :minorPersonUid 
    """)
    abstract suspend fun findByMinorPersonUid(minorPersonUid: Long): List<PersonParentJoin>

    /**
     * Represents a parent enrolment that needs to be done
     */
    data class ParentEnrolmentRequired(var parentPersonUid: Long = 0L, var clazzUid: Long = 0L)

    /**
     * Find classes for which a minor (child) is enroled where there is no parent enrolment for
     * the parent in the same class.
     */
    @Query("""
        SELECT PersonParentJoin.ppjParentPersonUid AS parentPersonUid,
               ChildEnrolment.clazzEnrolmentClazzUid AS clazzUid
          FROM PersonParentJoin
               JOIN ClazzEnrolment ChildEnrolment 
                    ON ChildEnrolment.clazzEnrolmentPersonUid = :minorPersonUid
                   AND (:clazzUidFilter = 0 OR ChildEnrolment.clazzEnrolmentClazzUid = :clazzUidFilter)
         WHERE PersonParentJoin.ppjMinorPersonUid = :minorPersonUid
           AND PersonParentJoin.ppjParentPersonUid != 0
           AND NOT EXISTS(
               SELECT clazzEnrolmentUid 
                 FROM ClazzEnrolment
                WHERE ClazzEnrolment.clazzEnrolmentPersonUid = PersonParentJoin.ppjParentPersonUid
                  AND ClazzEnrolment.clazzEnrolmentClazzUid = ChildEnrolment.clazzEnrolmentClazzUid
                  AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_PARENT}
                  AND CAST(ClazzEnrolment.clazzEnrolmentActive AS INTEGER) = 1)
    """)
    abstract suspend fun findByMinorPersonUidWhereParentNotEnrolledInClazz(
        minorPersonUid: Long,
        clazzUidFilter: Long
    ): List<ParentEnrolmentRequired>

    @Query("""
        SELECT EXISTS(
               SELECT ppjUid
                 FROM PersonParentJoin
                WHERE ppjMinorPersonUid = :minorPersonUid
                      AND ppjParentPersonUid = :userPersonUid
                      AND CAST(ppjInactive AS INTEGER) = 0)
    """)
    abstract suspend fun isParentOf(userPersonUid: Long, minorPersonUid: Long): Boolean

    @Update
    abstract suspend fun updateAsync(personParentJoin: PersonParentJoin)

    @Query("""
        SELECT EXISTS(
               SELECT ppjUid
                 FROM PersonParentJoin
                WHERE ppjMinorPersonUid = :minorPersonUid
                  AND CAST(ppjInactive AS INTEGER) = 0
                  AND ppjStatus = ${PersonParentJoin.STATUS_APPROVED})
    """)
    abstract suspend fun isMinorApproved(minorPersonUid: Long) : Boolean


}