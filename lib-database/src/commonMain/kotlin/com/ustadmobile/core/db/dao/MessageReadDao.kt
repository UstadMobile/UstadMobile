package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.SyncNode
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Message
import com.ustadmobile.lib.db.entities.Chat
import com.ustadmobile.lib.db.entities.MessageRead
import com.ustadmobile.lib.db.entities.MessageWithPerson
import com.ustadmobile.lib.db.entities.UserSession

@Dao
@Repository
abstract class MessageReadDao: BaseDao<MessageRead>{

    @Query("""
     REPLACE INTO MessageReadReplicate(messageReadPk, messageReadDestination)
      SELECT DISTINCT MessageRead.messageReadUid AS messageReadPk,
             :newNodeId AS messageReadDestination
        FROM UserSession 
              JOIN Message ON
                  ((    Message.messageTableId = ${Chat.TABLE_ID}
                    AND Message.messageEntityUid IN
                        (SELECT ChatMember.chatMemberChatUid 
                          FROM ChatMember
                         WHERE ChatMember.chatMemberPersonUid = UserSession.usPersonUid))
                  OR UserSession.usSessionType = ${UserSession.TYPE_UPSTREAM})
                  
              JOIN MessageRead 
                   ON MessageRead.messageReadMessageUid = Message.messageUid
                   
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND MessageRead.messageReadLct != COALESCE(
             (SELECT messageReadVersionId
                FROM MessageReadReplicate
               WHERE messageReadPk = MessageRead.messageReadUid
                 AND messageReadDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(messageReadPk, messageReadDestination) DO UPDATE
             SET messageReadPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([MessageRead::class])
    abstract suspend fun replicateOnNewNodeChats(@NewNodeIdParam newNodeId: Long)


    @Query("""
         REPLACE INTO MessageReadReplicate(messageReadPk, messageReadDestination)
          SELECT DISTINCT MessageRead.messageReadUid AS messageReadUid,
                 UserSession.usClientNodeId AS messageReadDestination
            FROM ChangeLog
            
                 JOIN MessageRead 
                      ON MessageRead.messageReadUid = ChangeLog.chEntityPk
                         AND ChangeLog.chTableId = ${MessageRead.TABLE_ID}
                         

                 JOIN UserSession ON
                      ((UserSession.usPersonUid IN 
                           (SELECT ChatMember.chatMemberPersonUid
                              FROM ChatMember
                             WHERE ChatMember.chatMemberChatUid = MessageRead.messageReadEntityUid))
                       OR UserSession.usSessionType = ${UserSession.TYPE_UPSTREAM})
                   
           WHERE UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
             AND UserSession.usClientNodeId != (
                 SELECT nodeClientId 
                   FROM SyncNode
                  LIMIT 1)
             AND MessageRead.messageReadLct != COALESCE(
                 (SELECT messageReadVersionId
                    FROM MessageReadReplicate
                   WHERE messageReadPk = MessageRead.messageReadUid
                     AND messageReadDestination = UserSession.usClientNodeId), 0)
         /*psql ON CONFLICT(messageReadPk, messageReadDestination) DO UPDATE
             SET messageReadPending = true
          */               
    """)
    @ReplicationRunOnChange([MessageRead::class])
    @ReplicationCheckPendingNotificationsFor([MessageRead::class])
    abstract suspend fun replicateOnChange()



}