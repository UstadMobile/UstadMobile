package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Chat
import com.ustadmobile.lib.db.entities.DiscussionTopic
import com.ustadmobile.lib.db.entities.DiscussionTopicListDetail
import com.ustadmobile.lib.db.entities.UserSession

@Dao
@Repository
abstract class DiscussionTopicDao: BaseDao<DiscussionTopic>{

    @Query("""
     REPLACE INTO DiscussionTopicReplicate(discussionTopicPk, discussionTopicDestination)
      SELECT DISTINCT DiscussionTopic.discussionTopicUid AS discussionTopicPk,
             :newNodeId AS discussionTopicDestination
             
       FROM DiscussionTopic
       WHERE DiscussionTopic.discussionTopicLct != COALESCE(
             (SELECT discussionTopicVersionId
                FROM discussionTopicReplicate
               WHERE discussionTopicPk = DiscussionTopic.discussionTopicUid
                 AND discussionTopicDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(discussionTopicPk, discussionTopicDestination) DO UPDATE
             SET discussionTopicPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([DiscussionTopic::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)


    @Query("""
        REPLACE INTO DiscussionTopicReplicate(discussionTopicPk, discussionTopicDestination)
          SELECT DISTINCT DiscussionTopic.discussionTopicUid AS discussionTopicUid,
                 UserSession.usClientNodeId AS discussionTopicDestination
            FROM ChangeLog
                 JOIN DiscussionTopic
                     ON ChangeLog.chTableId = ${DiscussionTopic.TABLE_ID}
                        AND ChangeLog.chEntityPk = DiscussionTopic.discussionTopicUid
                 JOIN UserSession ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
           WHERE UserSession.usClientNodeId != (
                 SELECT nodeClientId 
                   FROM SyncNode
                  LIMIT 1)
             AND DiscussionTopic.discussionTopicLct != COALESCE(
                 (SELECT discussionTopicVersionId
                    FROM discussionTopicReplicate
                   WHERE discussionTopicPk = DiscussionTopic.discussionTopicUid
                     AND DiscussionTopicDestination = UserSession.usClientNodeId), 0)
         /*psql ON CONFLICT(discussionTopicPk, discussionTopicDestination) DO UPDATE
             SET discussionTopicPending = true
          */               
    """)
    @ReplicationRunOnChange([DiscussionTopic::class])
    @ReplicationCheckPendingNotificationsFor([DiscussionTopic::class])
    abstract suspend fun replicateOnChange()



    @Query("""
        SELECT DiscussionTopic.*,
                0 as numThreads,
                0 as lastActiveTimestamp
          FROM DiscussionTopic     
         WHERE DiscussionTopic.discussionTopicCourseDiscussionUid = :discussionUid 
      ORDER BY DiscussionTopic.discussionTopicStartDate DESC
    """)
    abstract fun getListOfTopicsByDiscussion(discussionUid: Long)
        : DoorDataSourceFactory<Int, DiscussionTopicListDetail>

    @Query("""
        SELECT DiscussionTopic.*
          FROM DiscussionTopic
         WHERE DiscussionTopic.discussionTopicCourseDiscussionUid IN 
                (SELECT CourseDiscussion.courseDiscussionUid 
                   FROM CourseDiscussion
                  WHERE CourseDiscussion.courseDiscussionClazzUid = :clazzUid ) 
                        
    """)
    abstract suspend fun getTopicsByClazz(clazzUid: Long)
        : List<DiscussionTopic>


}