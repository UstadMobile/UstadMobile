package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.core.db.dao.UserSessionDaoCommon.FIND_LOCAL_SESSIONS_SQL
import kotlinx.coroutines.flow.Flow
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*

@DoorDao
@Repository
expect abstract class UserSessionDao {

    @Insert
    abstract suspend fun insertSession(session: UserSession): Long

    @Query("""
        SELECT UserSession.*
          FROM UserSession
         WHERE usPersonUid = :personUid 
    """)
    abstract suspend fun findSessionsByPerson(personUid: Long): List<UserSession>

    @Query(FIND_LOCAL_SESSIONS_SQL)
    abstract fun findAllLocalSessionsLive(): Flow<List<UserSessionAndPerson>>

    @Query(FIND_LOCAL_SESSIONS_SQL)
    abstract suspend fun findAllLocalSessionsAsync(): List<UserSessionAndPerson>

    @Query("""
            SELECT UserSession.*, Person.*
              FROM UserSession
                   JOIN Person ON UserSession.usPersonUid = Person.personUid
             WHERE Person.username = :username
               AND UserSession.usClientNodeId = (
                   SELECT COALESCE(
                          (SELECT nodeClientId 
                            FROM SyncNode
                           LIMIT 1), 0))
               AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}        
            """)
    abstract suspend fun findLocalSessionByUsername(username: String?): UserSessionAndPerson?

    /**
     * Count sessions on this device. If maxDateOfBirth is non-zero, then this can be used to
     * provide a cut-off (e.g. to find only sessions for adults where their date of birth must be
     * before a cut-off)
     *
     * This will not include any temporary local sessions
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
           AND (UserSession.usSessionType & ${UserSession.TYPE_TEMP_LOCAL}) != ${UserSession.TYPE_TEMP_LOCAL}            
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
    abstract fun findByUidLive(sessionUid: Long): Flow<UserSession?>


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