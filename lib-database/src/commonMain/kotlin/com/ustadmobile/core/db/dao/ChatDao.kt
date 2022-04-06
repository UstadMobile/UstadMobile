package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.SyncNode
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Chat
import com.ustadmobile.lib.db.entities.ChatWithLatestMessageAndCount
import com.ustadmobile.lib.db.entities.UserSession

@Dao
@Repository
abstract class ChatDao: BaseDao<Chat>{

    @Query("""
     REPLACE INTO chatReplicate(chatPk, chatDestination)
      SELECT DISTINCT Chat.chatUid AS chatPk,
             :newNodeId AS chatDestination
        FROM UserSession 
             JOIN Chat ON Chat.chatUid IN 
                ( (  
                SELECT ChatMember.chatMemberChatUid 
                  FROM ChatMember
                 WHERE ChatMember.chatMemberPersonUid = UserSession.usPersonUid
                 )
                 OR UserSession.usSessionType = ${UserSession.TYPE_UPSTREAM}
                 )
                 AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE} 
                 
       WHERE UserSession.usClientNodeId = :newNodeId
       AND Chat.chatLct != COALESCE(
             (SELECT chatVersionId
                FROM chatReplicate
               WHERE chatPk = Chat.chatUid
                 AND chatDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(chatPk, chatDestination) DO UPDATE
             SET chatPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([Chat::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)


    @Query("""
         REPLACE INTO chatReplicate(chatPk, chatDestination)
          SELECT DISTINCT Chat.chatUid AS chatUid,
                 UserSession.usClientNodeId AS chatDestination
            FROM ChangeLog
                 JOIN chat
                     ON ChangeLog.chTableId = ${Chat.TABLE_ID}
                        AND ChangeLog.chEntityPk = Chat.chatUid
                 JOIN UserSession ON UserSession.usPersonUid IN 
                      ( (
                      SELECT ChatMember.chatMemberPersonUid 
                        FROM ChatMember 
                       WHERE ChatMember.chatMemberChatUid = Chat.chatUid 
                      ) 
                      OR UserSession.usSessionType = ${UserSession.TYPE_UPSTREAM} )
                      AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
           WHERE UserSession.usClientNodeId != (
                 SELECT nodeClientId 
                   FROM SyncNode
                  LIMIT 1)
             AND Chat.chatLct != COALESCE(
                 (SELECT chatVersionId
                    FROM chatReplicate
                   WHERE chatPk = Chat.chatUid
                     AND chatDestination = UserSession.usClientNodeId), 0)
         /*psql ON CONFLICT(chatPk, chatDestination) DO UPDATE
             SET chatPending = true
          */               
    """)
    @ReplicationRunOnChange([Chat::class])
    @ReplicationCheckPendingNotificationsFor([Chat::class])
    abstract suspend fun replicateOnChange()


    @Insert
    abstract suspend fun updateAsync(entity: Chat)

    //TODO: Document the logic a bit
    @Query("""
        WITH Chats AS
            (SELECT Chat.*,
                    Message.messageText AS latestMessage,
                    Message.messageTimestamp AS latestMessageTimestamp,
                    op.personUid AS otherPersonUid,
                    op.firstNames AS otherPersonFirstNames,
                    op.lastName AS otherPersonLastName,
                    (0) AS unreadMessageCount,
        
                 (SELECT COUNT(*)
                  FROM ChatMember mm
                  WHERE mm.chatMemberChatUid = Chat.chatUid ) AS numMembers
             FROM ChatMember
             LEFT JOIN Chat ON Chat.chatUid = ChatMember.chatMemberChatUid
             LEFT JOIN Message ON Message.messageUid =
                 (SELECT messageUid
                  FROM Message
                  WHERE messageEntityUid = Chat.chatUid
                      AND messageTableId = ${Chat.TABLE_ID}
                  ORDER BY messageTimestamp DESC
                  LIMIT 1)
             LEFT JOIN Person op ON op.personUid =
                 (SELECT pp.personUid
                  FROM ChatMember cm
                  LEFT JOIN Person pp ON pp.personUid = cm.chatMemberPersonUid
                  WHERE cm.chatMemberChatUid = Chat.chatUid
                      AND cm.chatMemberPersonUid != :personUid
                      AND cm.chatMemberLeftDate = ${Long.MAX_VALUE}
                  LIMIT 1)
             WHERE ChatMember.chatMemberPersonUid = :personUid
                 AND ChatMember.chatMemberLeftDate = ${Long.MAX_VALUE}
                 AND Chat.chatUid != 0 
                 
             ORDER BY Message.messageTimestamp DESC)
        SELECT Chats.*
        FROM Chats
        UNION
        SELECT *
        FROM
            (SELECT Chat.*,
                    '' AS latestMessage,
                    0 AS latestMessageTimestamp,
                    Person.personUid AS otherPersonUid,
                    Person.firstNames AS otherPersonFirstNames,
                    Person.lastName AS otherPersonLastName,
                    0 AS unreadMessageCount,
                    0 AS numMembers
             FROM Person
             LEFT JOIN Chat ON Chat.chatUid = 0
             WHERE :searchBit != '%'
                 AND Person.personUid != :personUid
                 AND Person.personUid NOT IN
                     (SELECT p.personUid
                      FROM ChatMember c
                      LEFT JOIN Person p ON p.personUid = c.chatMemberPersonUid)
                 AND Person.firstNames||' '||Person.lastName LIKE :searchBit )
    """)
    abstract fun findAllChatsForUser(searchBit: String, personUid: Long)
        : DoorDataSourceFactory<Int, ChatWithLatestMessageAndCount>


    @Query("""
        SELECT CASE
                   WHEN Chat.isChatGroup THEN Chat.chatTitle
                   ELSE Person.firstNames||' '||Person.lastName
               END AS title
        FROM Chat
        LEFT JOIN Person ON Person.personUid =
          (SELECT pp.personUid
           FROM ChatMember cm
           LEFT JOIN Person pp ON pp.personUid = cm.chatMemberPersonUid
           WHERE cm.chatMemberChatUid = Chat.chatUid
             AND cm.chatMemberPersonUid != :personUid
             AND cm.chatMemberLeftDate = ${Long.MAX_VALUE}
           LIMIT 1)
        WHERE Chat.chatUid = :chatUid
    """)
    abstract suspend fun getTitleChat(chatUid: Long, personUid: Long): String?



}