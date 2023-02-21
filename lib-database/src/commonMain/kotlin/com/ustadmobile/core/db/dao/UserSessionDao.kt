package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.core.db.dao.UserSessionDaoCommon.FIND_LOCAL_SESSIONS_SQL
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*

@DoorDao
@Repository
expect abstract class UserSessionDao {

    /*
     * Here UserSessionSubject represents the UserSession for which we are checking access permissions
     * to decide whether or not to replicate. UserSession represents the UserSessions being used to
     * determine if permission is granted.
     */
    @Query("""
        REPLACE INTO UserSessionReplicate(usPk, usDestination)
         SELECT DISTINCT UserSessionSubject.usUid AS usPk,
                UserSession.usClientNodeId AS usDestination
           FROM ChangeLog
                JOIN UserSession UserSessionSubject
                     ON ChangeLog.chTableId = ${UserSession.TABLE_ID}
                        AND ChangeLog.chEntityPk = UserSessionSubject.usUid
                        AND UserSessionSubject.usSessionType = ${UserSession.TYPE_STANDARD}
                JOIN Person
                     ON UserSessionSubject.usPersonUid = Person.personUid
                ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_PERSON_SELECT}
                    /* Modify second part of query - remove requirement for session to be active.
                     * This ensures that deactivations are distributed
                     */
                    ) > 0
                     JOIN PersonGroupMember AS PrsGrpMbr
                          ON ScopedGrant.sgGroupUid = PrsGrpMbr.groupMemberGroupUid
                     JOIN UserSession
                          ON UserSession.usPersonUid = PrsGrpMbr.groupMemberPersonUid
          WHERE UserSessionSubject.usClientNodeId = UserSessionSubject.usClientNodeId                
          --notpsql              
            AND UserSessionSubject.usLct != COALESCE(
                (SELECT usVersionId
                   FROM UserSessionReplicate
                  WHERE UserSessionReplicate.usPk = UserSessionSubject.usUid
                    AND UserSessionReplicate.usDestination = UserSession.usClientNodeId), 0)
          --endnotpsql                       
        /*psql ON CONFLICT(usPk, usDestination) 
                DO UPDATE SET usPending = 
                   (SELECT UserSession.usLct
                      FROM UserSession
                     WHERE UserSession.usUid = EXCLUDED.usPk ) 
                        != UserSessionReplicate.usVersionId
         */         
    """)
    @ReplicationRunOnChange(value = [UserSession::class])
    @ReplicationCheckPendingNotificationsFor([UserSession::class])
    abstract suspend fun updateReplicationTrackers()

    /*
     * Here UserSessionSubject represents the UserSession for which we are checking access permissions
     * to decide whether or not to replicate. UserSession represents the UserSessions being used to
     * determine if permission is granted.
     */
    @Query("""
        REPLACE INTO UserSessionReplicate(usPk, usDestination)
         SELECT DISTINCT UserSessionSubject.usUid AS usPk,
                UserSession.usClientNodeId AS usDestination
           FROM UserSession 
                JOIN PersonGroupMember
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
                ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_PERSON_SELECT}
                    ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
                JOIN UserSession UserSessionSubject
                     ON UserSessionSubject.usPersonUid = Person.personUid
                        AND UserSessionSubject.usSessionType = ${UserSession.TYPE_STANDARD}
                        AND UserSessionSubject.usClientNodeId = :newNodeId
          WHERE UserSession.usClientNodeId = :newNodeId
          --notpsql
            AND UserSessionSubject.usLct != COALESCE(
                (SELECT usVersionId
                   FROM UserSessionReplicate
                  WHERE UserSessionReplicate.usPk = UserSessionSubject.usUid
                    AND UserSessionReplicate.usDestination = UserSession.usClientNodeId), 0)
          --endnotpsql          
         /*psql ON CONFLICT(usPk, usDestination) 
                DO UPDATE SET usPending = 
                   (SELECT UserSession.usLct
                      FROM UserSession
                     WHERE UserSession.usUid = EXCLUDED.usPk ) 
                        != UserSessionReplicate.usVersionId
         */
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([UserSession::class])
    abstract suspend fun updateReplicationTrackersOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Insert
    abstract suspend fun insertSession(session: UserSession): Long

    @Query("""
        SELECT UserSession.*
          FROM UserSession
         WHERE usPersonUid = :personUid 
    """)
    abstract suspend fun findSessionsByPerson(personUid: Long): List<UserSession>

    @Query(FIND_LOCAL_SESSIONS_SQL)
    abstract fun findAllLocalSessionsLive(): LiveData<List<UserSessionAndPerson>>

    @Query(FIND_LOCAL_SESSIONS_SQL)
    abstract suspend fun findAllLocalSessionsAsync(): List<UserSessionAndPerson>

    /**
     * Count sessions on this device. If maxDateOfBirth is non-zero, then this can be used to
     * provide a cut-off (e.g. to find only sessions for adults where their date of birth must be
     * before a cut-off)
     */
    @Query("""
        SELECT COUNT(*)
          FROM UserSession
               JOIN Person 
                    ON UserSession.usPersonUid = Person.personUid
         WHERE UserSession.usClientNodeId = (
                   SELECT COALESCE(
                          (SELECT nodeClientId 
                            FROM SyncNode
                           LIMIT 1), 0))
           AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}                
           AND (:maxDateOfBirth = 0 OR Person.dateOfBirth < :maxDateOfBirth)                 
    """)
    abstract suspend fun countAllLocalSessionsAsync(maxDateOfBirth: Long): Int

    @Query("""
        UPDATE UserSession
           SET usAuth = null,
               usStatus = :newStatus,
               usReason = :reason,
               usLcb = COALESCE(
                               (SELECT nodeClientId
                                  FROM SyncNode
                                 LIMIT 1), 0)
         WHERE UserSession.usUid = :sessionUid                        
               
    """)
    abstract suspend fun endSession(sessionUid: Long, newStatus: Int, reason: Int)

    @Query("""
        SELECT UserSession.*
          FROM UserSession
         WHERE UserSession.usUid = :sessionUid
         LIMIT 1
    """)
    abstract fun findByUidLive(sessionUid: Long): LiveData<UserSession?>


    @Query("""
        UPDATE UserSession
           SET usAuth = null,
               usStatus = :newStatus,
               usReason = :reason,
               usLct = :changeTime
         WHERE usPersonUid = :personUid
           AND usClientNodeId != :exemptNodeId
           AND usStatus != :newStatus                     
    """)
    abstract suspend fun endOtherSessions(
        personUid: Long,
        exemptNodeId: Long,
        newStatus: Int,
        reason: Int,
        changeTime: Long
    )

    @Query("""
        SELECT DISTINCT UserSession.usClientNodeId
          FROM UserSession
         WHERE UserSession.usPersonUid IN (:personUids)
           AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
    """)
    abstract suspend fun findActiveNodeIdsByPersonUids(personUids: List<Long>): List<Long>

    @Query("""
        SELECT DISTINCT UserSession.usClientNodeId
          FROM UserSession
               JOIN PersonGroupMember 
                    ON PersonGroupMember.groupMemberPersonUid = UserSession.usPersonUid
         WHERE PersonGroupMember.groupMemberGroupUid IN (:groupUids)            
    """)
    abstract suspend fun findActiveNodesIdsByGroupUids(groupUids: List<Long>): List<Long>

    /**
     * This query will find the nodeids for all users where the device has permissions that are
     * influenced by a given class.
     */
    @Query("""
        SELECT UserSession.usClientNodeId
          FROM ScopedGrant
               JOIN PersonGroupMember 
                    ON PersonGroupMember.groupMemberGroupUid = ScopedGrant.sgGroupUid
               JOIN UserSession
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
         WHERE (ScopedGrant.sgTableId = ${Clazz.TABLE_ID} AND ScopedGrant.sgEntityUid IN (:clazzUids))
            OR (ScopedGrant.sgTableId = ${School.TABLE_ID} AND ScopedGrant.sgEntityUid IN 
                (SELECT clazzSchoolUid
                   FROM Clazz
                  WHERE clazzUid IN (:clazzUids)))
          
    """)
    abstract suspend fun findAllActiveNodeIdsWithClazzBasedPermission(clazzUids: List<Long>): List<Long>

    @Query("""
        SELECT UserSession.usClientNodeId
          FROM ScopedGrant
               JOIN PersonGroupMember 
                    ON PersonGroupMember.groupMemberGroupUid = ScopedGrant.sgGroupUid
               JOIN UserSession
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
         WHERE ScopedGrant.sgTableId = ${School.TABLE_ID} 
           AND ScopedGrant.sgEntityUid IN (:schoolUids) 
    """)
    abstract suspend fun findAllActiveNodeIdsWithSchoolBasedPermission(schoolUids: List<Long>): List<Long>

}