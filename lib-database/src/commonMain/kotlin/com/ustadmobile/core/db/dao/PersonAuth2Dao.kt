package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonAuth2
import com.ustadmobile.lib.db.entities.Role

@DoorDao
@Repository
expect abstract class PersonAuth2Dao {

    @Query("""
     REPLACE INTO PersonAuth2Replicate(paPk, paDestination)
      SELECT DISTINCT PersonAuth2.pauthUid AS paUid,
             :newNodeId AS paDestination
        FROM UserSession
        JOIN PersonGroupMember
             ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
        ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
                ${Role.PERMISSION_AUTH_SELECT}
                ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
        JOIN PersonAuth2
             ON PersonAuth2.pauthUid = Person.personUid
       WHERE UserSession.usClientNodeId = :newNodeId      
         AND PersonAuth2.pauthLct != COALESCE(
             (SELECT paVersionId
                FROM PersonAuth2Replicate
               WHERE paPk = PersonAuth2.pauthUid
                 AND paDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(paPk, paDestination) DO UPDATE
             SET paPending = true
      */       
 """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([PersonAuth2::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

     @Query("""
 REPLACE INTO PersonAuth2Replicate(paPk, paDestination)
  SELECT DISTINCT PersonAuth2.pauthUid AS paUid,
         UserSession.usClientNodeId AS paDestination
    FROM ChangeLog
         JOIN PersonAuth2
             ON ChangeLog.chTableId = 678
                AND ChangeLog.chEntityPk = PersonAuth2.pauthUid
         JOIN Person
              ON Person.personUid = PersonAuth2.pauthUid
         ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_AUTH_SELECT}
              ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND PersonAuth2.pauthLct != COALESCE(
         (SELECT paVersionId
            FROM PersonAuth2Replicate
           WHERE paPk = PersonAuth2.pauthUid
             AND paDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(paPk, paDestination) DO UPDATE
     SET paPending = true
  */               
    """)
    @ReplicationRunOnChange([PersonAuth2::class])
    @ReplicationCheckPendingNotificationsFor([PersonAuth2::class])
    abstract suspend fun replicateOnChange()


    @Insert
    abstract suspend fun insertListAsync(auths: List<PersonAuth2>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAsync(auth: PersonAuth2): Long

    @Query("""
        SELECT PersonAuth2.*
          FROM PersonAuth2
         WHERE PersonAuth2.pauthUid = :personUid 
    """)
    abstract suspend fun findByPersonUid(personUid: Long): PersonAuth2?

    @Query("""
        SELECT PersonAuth2.*
          FROM PersonAuth2
               JOIN Person ON PersonAuth2.pauthUid = Person.personUid
         WHERE Person.username = :username
    """)
    abstract suspend fun findByUsername(username: String): PersonAuth2?

}