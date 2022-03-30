package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*
import kotlin.js.JsName

@Repository
@Dao
abstract class CourseTerminologyDao : BaseDao<CourseTerminology> {

    @Query("""
    REPLACE INTO CourseTerminologyReplicate(ctPk, ctDestination)
      SELECT DISTINCT CourseTerminology.ctUid AS ctPk,
             :newNodeId AS ctDestination
        FROM UserSession
             JOIN PersonGroupMember 
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_CLAZZ_SELECT} 
                    ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
               JOIN CourseTerminology
                    ON CourseTerminology.ctUid = Clazz.clazzTerminologyUid                
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND CourseTerminology.ctLct != COALESCE(
             (SELECT ctVersionId
                FROM CourseTerminologyReplicate
               WHERE ctPk = CourseTerminology.ctUid
                 AND ctDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(ctPk, ctDestination) DO UPDATE
             SET ctPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([CourseTerminology::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)



    @Query("""
         REPLACE INTO CourseTerminologyReplicate(ctPk, ctDestination)
  SELECT DISTINCT CourseTerminology.ctUid AS ctPk,
         UserSession.usClientNodeId AS ctDestination
    FROM ChangeLog
         JOIN CourseTerminology
             ON ChangeLog.chTableId = ${CourseTerminology.TABLE_ID}
                AND ChangeLog.chEntityPk = CourseTerminology.ctUid
             JOIN Clazz
                    ON  Clazz.clazzTerminologyUid = CourseTerminology.ctUid
         ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_CLAZZ_SELECT}
              ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}  
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND CourseTerminology.ctLct != COALESCE(
         (SELECT ctVersionId
            FROM CourseTerminologyReplicate
           WHERE ctPk = CourseTerminology.ctUid
             AND ctDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(ctPk, ctDestination) DO UPDATE
     SET ctPending = true
  */               
    """)
    @ReplicationRunOnChange([CourseBlock::class])
    @ReplicationCheckPendingNotificationsFor([CourseBlock::class])
    abstract suspend fun replicateOnChange()


    @Query("""
        SELECT *
         FROM CourseTerminology
     ORDER BY ctTitle   
    """)
    abstract fun findAllCourseTerminology(): DoorDataSourceFactory<Int, CourseTerminology>

    @Query("""
        SELECT *
         FROM CourseTerminology
     ORDER BY ctTitle   
    """)
    abstract fun findAllCourseTerminologyList(): List<CourseTerminology>


    @JsName("findByUid")
    @Query("""
        SELECT * 
         FROM CourseTerminology 
        WHERE ctUid = :uid
        """)
    abstract suspend fun findByUidAsync(uid: Long): CourseTerminology?

    @Update
    abstract suspend fun updateAsync(entity: CourseTerminology): Int

}