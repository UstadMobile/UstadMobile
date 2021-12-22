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
                JOIN Person
                     ON UserSessionSubject.usPersonUid = Person.personUid
                ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1} ${Role.PERMISSION_PERSON_SELECT}) > 0
                JOIN UserSession
                     ON UserSession.usPersonUid = Person.personUid
                        AND (:newNodeId = 0 OR UserSession.usClientNodeId = :newNodeId)
                */
    @Query("""
        REPLACE INTO UserSessionTrkr(usForeignKey, usVersionId, usDestination)
         SELECT UserSessionSubject.usUid AS usForeignKey,
                UserSessionSubject.usLct AS usVersionId,
                /*UserSession.usClientNodeId AS usDestination */
                DoorNode.nodeId AS usDestination
           FROM ChangeLog
                JOIN UserSession UserSessionSubject
                     ON ChangeLog.chTableId = ${UserSession.TABLE_ID}
                        AND ChangeLog.chEntityPk = UserSessionSubject.usUid
                JOIN DoorNode
                     ON (:newNodeId = 0 OR DoorNode.nodeId = :newNodeId)
                        
          WHERE UserSessionSubject.usLct != COALESCE(
                (SELECT usVersionId
                   FROM UserSessionTrkr
                  WHERE UserSessionTrkr.usForeignKey = UserSessionSubject.usUid
                    AND UserSessionTrkr.usDestination = DoorNode.nodeId/*UserSession.usClientNodeId*/), 0)
    """)
    @ReplicationRunOnChange(value = [UserSession::class])
    @ReplicationCheckPendingNotificationsFor([UserSession::class])
    abstract suspend fun updateReplicationTrackers(@NewNodeIdParam newNodeId: Long)

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