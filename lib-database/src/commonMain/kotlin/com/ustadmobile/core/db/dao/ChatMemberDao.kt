package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.SyncNode
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ChatMember
import com.ustadmobile.lib.db.entities.UserSession

@DoorDao
@Repository
expect abstract class ChatMemberDao: BaseDao<ChatMember>{

    @Query("""
     REPLACE INTO chatMemberReplicate(chatMemberPk, chatMemberDestination)
      SELECT DISTINCT ChatMember.chatMemberUid AS chatMemberPk,
             :newNodeId AS chatMemberDestination
             
        FROM UserSession
            JOIN ChatMember 
                 ON ((ChatMember.chatMemberChatUid IN
                      (SELECT chatMemberInternal.chatMemberChatUid 
                         FROM ChatMember chatMemberInternal
                        WHERE chatMemberInternal.chatMemberPersonUid = UserSession.usPersonUid))
                     OR UserSession.usSessionType = ${UserSession.TYPE_UPSTREAM})
                 AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE} 
       WHERE ChatMember.chatMemberLct != COALESCE(
             (SELECT chatMemberVersionId
                FROM chatMemberReplicate
               WHERE chatMemberPk = ChatMember.chatMemberUid
                 AND chatMemberDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(chatMemberPk, chatMemberDestination) DO UPDATE
             SET chatMemberPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([ChatMember::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)


    @Query("""
         REPLACE INTO chatMemberReplicate(chatMemberPk, chatMemberDestination)
          SELECT DISTINCT ChatMember.chatMemberUid AS chatMemberUid,
                 UserSession.usClientNodeId AS chatMemberDestination
            FROM ChangeLog
                 JOIN chatMember
                     ON ChangeLog.chTableId = ${ChatMember.TABLE_ID}
                        AND ChangeLog.chEntityPk = ChatMember.chatMemberUid
                        
                 JOIN UserSession ON 
                      (UserSession.usSessionType = ${UserSession.TYPE_UPSTREAM}
                      OR (UserSession.usPersonUid IN 
                           (SELECT ChatMember.chatMemberPersonUid 
                              FROM ChatMember 
                             WHERE ChatMember.chatMemberChatUid IN 
                                   (SELECT ChatMemberInternal.chatMemberChatUid 
                                      FROM ChatMember ChatMemberInternal
                                     WHERE ChatMemberInternal.chatMemberPersonUid = 
                                           UserSession.usPersonUid))))
                      AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
                      
                
           WHERE UserSession.usClientNodeId != (
                 SELECT nodeClientId 
                   FROM SyncNode
                  LIMIT 1)
             AND ChatMember.chatMemberLct != COALESCE(
                 (SELECT chatMemberVersionId
                    FROM chatMemberReplicate
                   WHERE chatMemberPk = ChatMember.chatMemberUid
                     AND chatMemberDestination = UserSession.usClientNodeId), 0)
         /*psql ON CONFLICT(chatMemberPk, chatMemberDestination) DO UPDATE
             SET chatMemberPending = true
          */               
    """)
    @ReplicationRunOnChange([ChatMember::class])
    @ReplicationCheckPendingNotificationsFor([ChatMember::class])
    abstract suspend fun replicateOnChange()


}