package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*

@DoorDao
@Repository
expect abstract class DiscussionTopicDao: BaseDao<DiscussionTopic>, OneToManyJoinDao<DiscussionTopic>{

    @Query("""
     REPLACE INTO DiscussionTopicReplicate(discussionTopicPk, discussionTopicDestination)
      SELECT DISTINCT DiscussionTopic.discussionTopicUid AS discussionTopicPk,
             :newNodeId AS discussionTopicDestination
             
       FROM UserSession
             JOIN PersonGroupMember 
                  ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                  ${Role.PERMISSION_CLAZZ_SELECT} 
                  ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
                  
             JOIN DiscussionTopic 
                  ON DiscussionTopic.discussionTopicClazzUid = Clazz.clazzUid
                  
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
                        
                        
                 JOIN Clazz
                      ON Clazz.clazzUid = DiscussionTopic.discussionTopicClazzUid
                 ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
                  ${Role.PERMISSION_CLAZZ_SELECT}
                 ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}     
                 
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
                ( 
                    SELECT COUNT(*) 
                      FROM DiscussionPost 
                     WHERE DiscussionPost.discussionPostDiscussionTopicUid = DiscussionTopic.discussionTopicUid
                ) as numPosts,
                (
                    0
                )as lastActiveTimestamp
          FROM DiscussionTopic     
         WHERE DiscussionTopic.discussionTopicCourseDiscussionUid = :discussionUid 
           AND CAST(DiscussionTopic.discussionTopicVisible AS INTEGER) = 1
           AND CAST(DiscussionTopic.discussionTopicArchive AS INTEGER) = 0
      ORDER BY DiscussionTopic.discussionTopicIndex
    """)
    abstract fun getListOfTopicsByDiscussion(discussionUid: Long)
        : DataSourceFactory<Int, DiscussionTopicListDetail>

    @Query("""
        SELECT DiscussionTopic.*
          FROM DiscussionTopic
         WHERE DiscussionTopic.discussionTopicCourseDiscussionUid IN 
                (SELECT CourseDiscussion.courseDiscussionUid 
                   FROM CourseDiscussion
                  WHERE CourseDiscussion.courseDiscussionClazzUid = :clazzUid ) 
          AND CAST(DiscussionTopic.discussionTopicVisible AS INTEGER) = 1
          AND CAST(DiscussionTopic.discussionTopicArchive AS INTEGER)  = 0
                        
    """)
    abstract suspend fun getTopicsByClazz(clazzUid: Long)
        : List<DiscussionTopic>


    @Query("""
        SELECT DiscussionTopic.*
          FROM DiscussionTopic
         WHERE DiscussionTopic.discussionTopicUid = :discussionTopicUid
         
         """)
    abstract fun getDiscussionTopicByUid(discussionTopicUid: Long): LiveData<DiscussionTopic?>


    @Query("""
        UPDATE DiscussionTopic 
           SET discussionTopicVisible = :active, 
               discussionTopicLct = :changeTime
         WHERE discussionTopicUid = :uid""")
    abstract suspend fun updateActiveByUid(uid: Long, active: Boolean,  changeTime: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceListAsync(list: List<DiscussionTopic>)

}