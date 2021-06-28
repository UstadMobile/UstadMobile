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

    @Query("""
        SELECT UserSession.*, Person.*
          FROM UserSession
               JOIN Person ON UserSession.usPersonUid = Person.personUid
         WHERE UserSession.usClientNodeId = (
               SELECT COALESCE(
                      (SELECT nodeClientId 
                        FROM SyncNode
                       LIMIT 1), 0))
    """)
    abstract fun findAllLocalSessions(): DoorLiveData<List<UserSessionAndPerson>>


    @Query("""
        UPDATE UserSession
           SET usAuth = null,
               usStatus = :newStatus,
               usReason = :reason,
               usLcb = (SELECT COALESCE(
                               (SELECT nodeClientId
                                  FROM SyncNode
                                 LIMIT 1), 0))
               
    """)
    abstract suspend fun endSession(sessionUid: Long, newStatus: Int, reason: Int)

}