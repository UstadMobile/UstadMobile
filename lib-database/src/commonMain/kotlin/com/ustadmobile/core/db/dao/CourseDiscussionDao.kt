package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CourseDiscussion
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UserSession

@DoorDao
@Repository
expect abstract class CourseDiscussionDao: BaseDao<CourseDiscussion>, OneToManyJoinDao<CourseDiscussion>{

    @Query("""
     REPLACE INTO CourseDiscussionReplicate(courseDiscussionPk, courseDiscussionDestination)
      SELECT DISTINCT CourseDiscussion.courseDiscussionUid AS courseDiscussionPk,
             :newNodeId AS courseDiscussionDestination
             
       FROM UserSession
             JOIN PersonGroupMember 
                  ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                  ${Role.PERMISSION_CLAZZ_SELECT} 
                  ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
                  
             JOIN CourseDiscussion 
                  ON CourseDiscussion.courseDiscussionClazzUid = Clazz.clazzUid
                  
       WHERE CourseDiscussion.courseDiscussionLct != COALESCE(
             (SELECT courseDiscussionVersionId
                FROM courseDiscussionReplicate
               WHERE courseDiscussionPk = CourseDiscussion.courseDiscussionUid
                 AND courseDiscussionDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(courseDiscussionPk, courseDiscussionDestination) DO UPDATE
             SET courseDiscussionPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([CourseDiscussion::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)


    @Query("""
        REPLACE INTO CourseDiscussionReplicate(courseDiscussionPk, courseDiscussionDestination)
          SELECT DISTINCT CourseDiscussion.courseDiscussionUid AS courseDiscussionUid,
                 UserSession.usClientNodeId AS courseDiscussionDestination
            FROM ChangeLog
                 JOIN CourseDiscussion
                     ON ChangeLog.chTableId = ${CourseDiscussion.TABLE_ID}
                        AND ChangeLog.chEntityPk = CourseDiscussion.courseDiscussionUid
                 JOIN Clazz
                      ON Clazz.clazzUid = CourseDiscussion.courseDiscussionClazzUid
                 ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
                  ${Role.PERMISSION_CLAZZ_SELECT}
                 ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
                 
           WHERE UserSession.usClientNodeId != (
                 SELECT nodeClientId 
                   FROM SyncNode
                  LIMIT 1)
             AND CourseDiscussion.courseDiscussionLct != COALESCE(
                 (SELECT courseDiscussionVersionId
                    FROM courseDiscussionReplicate
                   WHERE courseDiscussionPk = CourseDiscussion.courseDiscussionUid
                     AND courseDiscussionDestination = UserSession.usClientNodeId), 0)
         /*psql ON CONFLICT(courseDiscussionPk, courseDiscussionDestination) DO UPDATE
             SET courseDiscussionPending = true
          */               
    """)
    @ReplicationRunOnChange([CourseDiscussion::class])
    @ReplicationCheckPendingNotificationsFor([CourseDiscussion::class])
    abstract suspend fun replicateOnChange()


    @Query("""
        UPDATE CourseDiscussion 
           SET courseDiscussionActive = :active, 
               courseDiscussionLct = :changeTime
         WHERE courseDiscussionUid = :cbUid""")
    abstract suspend fun updateActiveByUid(cbUid: Long, active: Boolean,  changeTime: Long)

    @Query("""
        SELECT CourseDiscussion.* 
          FROM CourseDiscussion
         WHERE CourseDiscussion.courseDiscussionUid = :courseDiscussionUid 
           AND CAST(CourseDiscussion.courseDiscussionActive AS INTEGER) = 1 
         
         """)
    abstract fun getCourseDiscussionByUid(courseDiscussionUid: Long): LiveData<CourseDiscussion?>



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceListAsync(list: List<CourseDiscussion>)

}