package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.DiscussionPost
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.lib.db.entities.DiscussionTopicListDetail
import com.ustadmobile.lib.db.entities.UserSession

@Dao
@Repository
abstract class DiscussionPostDao: BaseDao<DiscussionPost>{

    @Query("""
     REPLACE INTO DiscussionPostReplicate(discussionPostPk, discussionPostDestination)
      SELECT DISTINCT DiscussionPost.discussionPostUid AS discussionPostPk,
             :newNodeId AS discussionPostDestination
             
       FROM DiscussionPost
       WHERE DiscussionPost.discussionPostLct != COALESCE(
             (SELECT discussionPostVersionId
                FROM discussionPostReplicate
               WHERE discussionPostPk = DiscussionPost.discussionPostUid
                 AND discussionPostDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(discussionPostPk, discussionPostDestination) DO UPDATE
             SET discussionPostPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([DiscussionPost::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)


    @Query("""
        REPLACE INTO DiscussionPostReplicate(discussionPostPk, discussionPostDestination)
          SELECT DISTINCT DiscussionPost.discussionPostUid AS discussionPostUid,
                 UserSession.usClientNodeId AS discussionPostDestination
            FROM ChangeLog
                 JOIN DiscussionPost
                     ON ChangeLog.chTableId = ${DiscussionPost.TABLE_ID}
                        AND ChangeLog.chEntityPk = DiscussionPost.discussionPostUid
                 JOIN UserSession ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
           WHERE UserSession.usClientNodeId != (
                 SELECT nodeClientId 
                   FROM SyncNode
                  LIMIT 1)
             AND DiscussionPost.discussionPostLct != COALESCE(
                 (SELECT discussionPostVersionId
                    FROM discussionPostReplicate
                   WHERE discussionPostPk = DiscussionPost.discussionPostUid
                     AND DiscussionPostDestination = UserSession.usClientNodeId), 0)
         /*psql ON CONFLICT(discussionPostPk, discussionPostDestination) DO UPDATE
             SET discussionPostPending = true
          */               
    """)
    @ReplicationRunOnChange([DiscussionPost::class])
    @ReplicationCheckPendingNotificationsFor([DiscussionPost::class])
    abstract suspend fun replicateOnChange()


    //TODO
    @Query("""
        SELECT DiscussionPost.*,
            '' as authorPersonFirstNames,
            '' as authorPersonLastName,
            '' as postLatestMessage,
            0 as postRepliesCount, 
            0 as postLatestMessageTimestamp
             
          FROM DiscussionPost     
         WHERE DiscussionPost.discussionPostDiscussionTopicUid = :discussionTopicUid
           AND DiscussionPost.discussionPostVisible = 1
           AND DiscussionPost.discussionPostArchive = 0
      ORDER BY DiscussionPost.discussionPostStartDate DESC
    """)
    abstract fun getPostsByDiscussionTopic(discussionTopicUid: Long)
            : DoorDataSourceFactory<Int, DiscussionPostWithDetails>

    


}