package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.CourseDiscussion
import com.ustadmobile.lib.db.entities.UserSession

@Dao
@Repository
abstract class CourseDiscussionDao: BaseDao<CourseDiscussion>, OneToManyJoinDao<CourseDiscussion>{

    @Query("""
     REPLACE INTO CourseDiscussionReplicate(courseDiscussionPk, courseDiscussionDestination)
      SELECT DISTINCT CourseDiscussion.courseDiscussionUid AS courseDiscussionPk,
             :newNodeId AS courseDiscussionDestination
             
       FROM CourseDiscussion
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
                 JOIN UserSession ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
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
    abstract fun updateActiveByUid(cbUid: Long, active: Boolean,  changeTime: Long)

    override suspend fun deactivateByUids(uidList: List<Long>, changeTime: Long) {
        uidList.forEach {
            updateActiveByUid(it, false, changeTime)
        }
    }


    @Query("""
        SELECT CourseDiscussion.* 
          FROM CourseDiscussion
         WHERE CourseDiscussion.courseDiscussionUid = :courseDiscussionUid 
           AND CAST(CourseDiscussion.courseDiscussionActive AS INTEGER) = 1 
         
         """)
    abstract fun getCourseDiscussionByUid(courseDiscussionUid: Long): DoorLiveData<CourseDiscussion?>



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceListAsync(list: List<CourseDiscussion>)

}