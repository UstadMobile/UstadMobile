package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UserSession


@DoorDao
@Repository
expect abstract class PersonPictureDao : BaseDao<PersonPicture> {

    @Query("""
     REPLACE INTO PersonPictureReplicate(ppPk, ppDestination)
      SELECT DISTINCT PersonPicture.personPictureUid AS ppPk,
             :newNodeId AS ppDestination
        FROM UserSession
             JOIN PersonGroupMember
                  ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
                  ${Role.PERMISSION_PERSON_PICTURE_SELECT}
                  ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
             JOIN PersonPicture
                  ON PersonPicture.personPicturePersonUid = Person.personUid
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND PersonPicture.personPictureLct != COALESCE(
             (SELECT ppVersionId
                FROM PersonPictureReplicate
               WHERE ppPk = PersonPicture.personPictureUid
                 AND ppDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(ppPk, ppDestination) DO UPDATE
             SET ppPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([PersonPicture::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO PersonPictureReplicate(ppPk, ppDestination)
  SELECT DISTINCT PersonPicture.personPictureUid AS ppUid,
         UserSession.usClientNodeId AS ppDestination
    FROM ChangeLog
         JOIN PersonPicture
              ON ChangeLog.chTableId = ${PersonPicture.TABLE_ID}
                 AND ChangeLog.chEntityPk = PersonPicture.personPictureUid
         JOIN Person
              ON Person.personUid = PersonPicture.personPicturePersonUid
         ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_PERSON_PICTURE_SELECT}
              ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND PersonPicture.personPictureLct != COALESCE(
         (SELECT ppVersionId
            FROM PersonPictureReplicate
           WHERE ppPk = PersonPicture.personPictureUid
             AND ppDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(ppPk, ppDestination) DO UPDATE
     SET ppPending = true
  */               
    """)
    @ReplicationRunOnChange([PersonPicture::class])
    @ReplicationCheckPendingNotificationsFor([PersonPicture::class])
    abstract suspend fun replicateOnChange()

    @Query("""SELECT * FROM PersonPicture 
        WHERE personPicturePersonUid = :personUid
        AND CAST(personPictureActive AS INTEGER) = 1
        ORDER BY picTimestamp DESC LIMIT 1""")
    abstract suspend fun findByPersonUidAsync(personUid: Long): PersonPicture?

    @Query("SELECT * FROM PersonPicture where personPicturePersonUid = :personUid ORDER BY " + " picTimestamp DESC LIMIT 1")
    abstract fun findByPersonUidLive(personUid: Long): LiveData<PersonPicture?>


    @Update
    abstract suspend fun updateAsync(personPicture: PersonPicture)

}