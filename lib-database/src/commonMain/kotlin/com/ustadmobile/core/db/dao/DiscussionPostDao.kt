package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*

@Dao
@Repository
abstract class DiscussionPostDao: BaseDao<DiscussionPost>{

    @Query("""
     REPLACE INTO DiscussionPostReplicate(discussionPostPk, discussionPostDestination)
      SELECT DISTINCT DiscussionPost.discussionPostUid AS discussionPostPk,
             :newNodeId AS discussionPostDestination
             
       FROM UserSession
             JOIN PersonGroupMember 
                  ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                  ${Role.PERMISSION_CLAZZ_SELECT} 
                  ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
                  
            JOIN DiscussionPost 
                 ON DiscussionPost.discussionPostClazzUid = Clazz.clazzUid
                 
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
                        
                        
                 JOIN Clazz 
                      ON Clazz.clazzUid = DiscussionPost.discussionPostClazzUid
                      
                 ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
                  ${Role.PERMISSION_CLAZZ_SELECT}
                 ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
                 
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


    @Query("""
        SELECT DiscussionPost.*,
            Person.firstNames as authorPersonFirstNames,
            Person.lastName as authorPersonLastName,
            (
                SELECT Message.messageText 
                  FROM Message 
                 WHERE Message.messageTableId = ${DiscussionPost.TABLE_ID}
                   AND Message.messageEntityUid = DiscussionPost.discussionPostUid 
                 ORDER BY messageTimestamp 
                  DESC LIMIT 1
            ) AS postLatestMessage,
            (
                SELECT COUNT(*) 
                  FROM Message
                 WHERE Message.messageTableId = ${DiscussionPost.TABLE_ID}
                   AND Message.messageEntityUid = DiscussionPost.discussionPostUid 
                   
            ) AS postRepliesCount, 
            
            (
                SELECT Message.messageTimestamp 
                  FROM Message 
                 WHERE Message.messageTableId = ${DiscussionPost.TABLE_ID}
                   AND Message.messageEntityUid = DiscussionPost.discussionPostUid 
                 ORDER BY messageTimestamp 
                  DESC LIMIT 1
            ) AS postLatestMessageTimestamp
             
          FROM DiscussionPost     
          LEFT JOIN Person ON Person.personUid = DiscussionPost.discussionPostStartedPersonUid
         WHERE DiscussionPost.discussionPostDiscussionTopicUid = :discussionTopicUid
           AND CAST(DiscussionPost.discussionPostVisible AS INTEGER) = 1
           AND CAST(DiscussionPost.discussionPostArchive AS INTEGER) = 0
      ORDER BY DiscussionPost.discussionPostStartDate DESC
    """)
    abstract fun getPostsByDiscussionTopic(discussionTopicUid: Long)
            : DoorDataSourceFactory<Int, DiscussionPostWithDetails>


    @Query("""
        SELECT DiscussionPost.discussionPostTitle 
          FROM DiscussionPost 
         WHERE DiscussionPost.discussionPostUid = :postUid
    """)
    abstract suspend fun getPostTitle(postUid: Long): String?

    @Query("""
        SELECT * 
         FROM DiscussionPost
        WHERE DiscussionPost.discussionPostUid = :uid
    """)
    abstract suspend fun findByUid(uid: Long): DiscussionPost?


    @Query("""
        SELECT DiscussionPost.*,
            Person.firstNames as authorPersonFirstNames,
            Person.lastName as authorPersonLastName,
            '' AS postLatestMessage,
            0 AS postRepliesCount, 
            DiscussionPost.discussionPostLct AS postLatestMessageTimestamp
             
          FROM DiscussionPost     
          LEFT JOIN Person ON Person.personUid = DiscussionPost.discussionPostStartedPersonUid
         WHERE DiscussionPost.discussionPostUid = :uid
           
    """)
    abstract suspend fun findWithDetailsByUid(uid: Long): DiscussionPostWithDetails?

    @Query("""
        SELECT DiscussionPost.*,
            Person.firstNames as authorPersonFirstNames,
            Person.lastName as authorPersonLastName,
            '' AS postLatestMessage,
            0 AS postRepliesCount, 
            DiscussionPost.discussionPostLct AS postLatestMessageTimestamp
             
          FROM DiscussionPost     
          LEFT JOIN Person ON Person.personUid = DiscussionPost.discussionPostStartedPersonUid
         WHERE DiscussionPost.discussionPostUid = :uid
           
    """)
    abstract fun findWithDetailsByUidLive(uid: Long): DoorLiveData<DiscussionPostWithDetails?>

    @Update
    abstract suspend fun updateAsync(entity: DiscussionPost): Int

}