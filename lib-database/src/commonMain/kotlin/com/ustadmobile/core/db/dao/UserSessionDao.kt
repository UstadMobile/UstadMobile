package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.UserSession
import com.ustadmobile.lib.db.entities.UserSessionAndPerson

@Dao
@Repository
abstract class UserSessionDao {

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

    @Query("""
        SELECT COUNT(*)
          FROM UserSession
         WHERE UserSession.usClientNodeId = (
                   SELECT COALESCE(
                          (SELECT nodeClientId 
                            FROM SyncNode
                           LIMIT 1), 0))
           AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
    """)
    abstract suspend fun countAllLocalSessionsAsync(): Int

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