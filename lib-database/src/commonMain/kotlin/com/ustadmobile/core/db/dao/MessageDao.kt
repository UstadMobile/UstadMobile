package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.SyncNode
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Chat
import com.ustadmobile.lib.db.entities.Message
import com.ustadmobile.lib.db.entities.MessageWithPerson
import com.ustadmobile.lib.db.entities.UserSession

@Dao
@Repository
abstract class MessageDao: BaseDao<Message>{

    @Query("""
     REPLACE INTO MessageReplicate(messagePk, messageDestination)
      SELECT DISTINCT Message.messageUid AS messagePk,
             :newNodeId AS messageDestination
        FROM UserSession
             JOIN Message ON
                  ((    Message.messageTableId = ${Chat.TABLE_ID}
                    AND Message.messageEntityUid IN
                        (SELECT ChatMember.chatMemberChatUid 
                          FROM ChatMember
                         WHERE ChatMember.chatMemberPersonUid = UserSession.usPersonUid))
                  OR UserSession.usSessionType = ${UserSession.TYPE_UPSTREAM})
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND Message.messageLct != COALESCE(
             (SELECT messageVersionId
                FROM MessageReplicate
               WHERE messagePk = Message.messageUid
                 AND messageDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(messagePk, messageDestination) DO UPDATE
             SET messagePending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([Message::class])
    abstract suspend fun replicateOnNewNodeChats(@NewNodeIdParam newNodeId: Long)


    @Query("""
         REPLACE INTO MessageReplicate(messagePk, messageDestination)
          SELECT DISTINCT Message.messageUid AS messageUid,
                 UserSession.usClientNodeId AS messageDestination
            FROM ChangeLog
                 JOIN Message
                     ON ChangeLog.chTableId = ${Message.TABLE_ID}
                        AND ChangeLog.chEntityPk = Message.messageUid
                        AND Message.messageTableId = ${Chat.TABLE_ID}
                 JOIN UserSession ON
                      ((UserSession.usPersonUid IN 
                           (SELECT ChatMember.chatMemberPersonUid
                              FROM ChatMember
                             WHERE ChatMember.chatMemberChatUid = Message.messageEntityUid))
                       OR UserSession.usSessionType = ${UserSession.TYPE_UPSTREAM})       
           WHERE UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
             AND UserSession.usClientNodeId != (
                 SELECT nodeClientId 
                   FROM SyncNode
                  LIMIT 1)
             AND Message.messageLct != COALESCE(
                 (SELECT messageVersionId
                    FROM MessageReplicate
                   WHERE messagePk = Message.messageUid
                     AND messageDestination = UserSession.usClientNodeId), 0)
         /*psql ON CONFLICT(messagePk, messageDestination) DO UPDATE
             SET messagePending = true
          */               
    """)
    @ReplicationRunOnChange([Message::class])
    @ReplicationCheckPendingNotificationsFor([Message::class])
    abstract suspend fun replicateOnChange()


    @Query("""
        SELECT Message.*, Person.* FROM Message
        LEFT JOIN Person ON Message.messageSenderPersonUid = Person.personUid
        WHERE Message.messageTableId = :tableId 
          AND Message.messageEntityUid = :entityUid
        ORDER BY Message.messageTimestamp DESC
    """)
    abstract fun findAllMessagesByChatUid(entityUid: Long, tableId: Int):
            DoorDataSourceFactory<Int, MessageWithPerson>


}