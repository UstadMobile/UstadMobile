package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CoursePicture
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UserSession


@DoorDao
@Repository
expect abstract class CoursePictureDao : BaseDao<CoursePicture> {

    @Query("""
     REPLACE INTO CoursePictureReplicate(cpPk, cpDestination)
      SELECT DISTINCT CoursePicture.coursePictureUid AS cpPk,
             :newNodeId AS cpDestination
        FROM UserSession
             JOIN PersonGroupMember
                  ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
                 
              ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_CLAZZ_SELECT} 
                    ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
                 
             JOIN CoursePicture
                  ON CoursePicture.coursePictureClazzUid = Clazz.clazzUid
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND CoursePicture.coursePictureLct != COALESCE(
             (SELECT cpVersionId
                FROM CoursePictureReplicate
               WHERE cpPk = CoursePicture.coursePictureUid
                 AND cpDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(cpPk, cpDestination) DO UPDATE
             SET cpPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([CoursePicture::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)


    @Query("""
 REPLACE INTO CoursePictureReplicate(cpPk, cpDestination)
  SELECT DISTINCT CoursePicture.coursePictureUid AS cpUid,
         UserSession.usClientNodeId AS cpDestination
    FROM ChangeLog
         JOIN CoursePicture
              ON ChangeLog.chTableId = ${CoursePicture.TABLE_ID}
                 AND ChangeLog.chEntityPk = CoursePicture.coursePictureUid
        JOIN Clazz 
            ON CoursePicture.coursePictureClazzUid = Clazz.clazzUid
  
        ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_CLAZZ_SELECT} 
                    
           
        ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
        
 
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND CoursePicture.coursePictureLct != COALESCE(
         (SELECT cpVersionId
            FROM CoursePictureReplicate
           WHERE cpPk = CoursePicture.coursePictureUid
             AND cpDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(cpPk, cpDestination) DO UPDATE
     SET cpPending = true
  */               
    """)
    @ReplicationRunOnChange([CoursePicture::class])
    @ReplicationCheckPendingNotificationsFor([CoursePicture::class])
    abstract suspend fun replicateOnChange()

    @Query("""SELECT * FROM CoursePicture 
        WHERE coursePictureClazzUid = :clazzUid
        AND CAST(coursePictureActive AS INTEGER) = 1
        ORDER BY coursePictureTimestamp DESC LIMIT 1""")
    abstract suspend fun findByClazzUidAsync(clazzUid: Long): CoursePicture?

    @Query("SELECT * FROM CoursePicture where coursePictureClazzUid = :clazzUid ORDER BY " + " coursePictureTimestamp DESC LIMIT 1")
    abstract fun findByClazzUidLive(clazzUid: Long): LiveData<CoursePicture?>


    @Update
    abstract suspend fun updateAsync(coursePicture: CoursePicture)



}