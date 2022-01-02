package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UserSession
import com.ustadmobile.lib.db.entities.UserSessionAndPerson

@Dao
@Repository
abstract class UserSessionDao {

    /*
     * Here UserSessionSubject represents the UserSession for which we are checking access permissions
     * to decide whether or not to replicate. UserSession represents the UserSessions being used to
     * determine if permission is granted.
     */
    @Query("""
        REPLACE INTO UserSessionReplicate(usPk, usVersionId, usDestination)
         SELECT UserSessionSubject.usUid AS usPk,
                UserSessionSubject.usLct AS usVersionId,
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
                    ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}     
                        
          WHERE UserSessionSubject.usLct != COALESCE(
                (SELECT usVersionId
                   FROM UserSessionReplicate
                  WHERE UserSessionReplicate.usPk = UserSessionSubject.usUid
                    AND UserSessionReplicate.usDestination = UserSession.usClientNodeId), 0)
        /*psql ON CONFLICT(usPk, usDestination) DO UPDATE
                SET usProcessed = false, usVersionId = EXCLUDED.usVersionId
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
        REPLACE INTO UserSessionReplicate(usPk, usVersionId, usDestination)
         SELECT UserSessionSubject.usUid AS usPk,
                UserSessionSubject.usLct AS usVersionId,
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
          WHERE UserSession.usClientNodeId = :newNodeId
            AND UserSessionSubject.usLct != COALESCE(
                (SELECT usVersionId
                   FROM UserSessionReplicate
                  WHERE UserSessionReplicate.usPk = UserSessionSubject.usUid
                    AND UserSessionReplicate.usDestination = UserSession.usClientNodeId), 0)
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
    abstract fun findAllLocalSessionsLive(): DoorLiveData<List<UserSessionAndPerson>>

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
               usLcb = (SELECT COALESCE(
                               (SELECT nodeClientId
                                  FROM SyncNode
                                 LIMIT 1), 0))
         WHERE UserSession.usUid = :sessionUid                        
               
    """)
    abstract suspend fun endSession(sessionUid: Long, newStatus: Int, reason: Int)

    @Query("""
        SELECT UserSession.*
          FROM UserSession
         WHERE UserSession.usUid = :sessionUid
         LIMIT 1
    """)
    abstract fun findByUidLive(sessionUid: Long): DoorLiveData<UserSession?>


    @Query("""
        UPDATE UserSession
           SET usAuth = null,
               usStatus = :newStatus,
               usReason = :reason,
               usLcb = (SELECT COALESCE(
                               (SELECT nodeClientId
                                  FROM SyncNode
                                 LIMIT 1), 0))
         WHERE usPersonUid = :personUid
           AND usClientNodeId != :exemptNodeId
           AND usStatus != :newStatus                     
    """)
    abstract suspend fun endOtherSessions(personUid: Long, exemptNodeId: Long, newStatus: Int,
                                          reason: Int)

    @Query("""
        SELECT UserSession.usClientNodeId
          FROM UserSession
         WHERE UserSession.usPersonUid IN (:personUids)
           AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
    """)
    abstract suspend fun findActiveNodeIdsByPersonUids(personUids: List<Long>): List<Long>

    companion object {
        const val FIND_LOCAL_SESSIONS_SQL = """
            SELECT UserSession.*, Person.*
              FROM UserSession
                   JOIN Person ON UserSession.usPersonUid = Person.personUid
             WHERE UserSession.usClientNodeId = (
                   SELECT COALESCE(
                          (SELECT nodeClientId 
                            FROM SyncNode
                           LIMIT 1), 0))
               AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}        
            """
    }

}