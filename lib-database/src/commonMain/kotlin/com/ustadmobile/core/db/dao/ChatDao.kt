package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.SyncNode
import com.ustadmobile.door.annotation.*
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.lib.db.entities.*

@DoorDao
@Repository
expect abstract class ChatDao: BaseDao<Chat>{

    @Query("""
     REPLACE INTO chatReplicate(chatPk, chatDestination)
      SELECT DISTINCT Chat.chatUid AS chatPk,
             :newNodeId AS chatDestination
        FROM UserSession 
             JOIN Chat ON 
                  ((Chat.chatUid IN 
                       (SELECT ChatMember.chatMemberChatUid 
                          FROM ChatMember
                         WHERE ChatMember.chatMemberPersonUid = UserSession.usPersonUid))
                   OR UserSession.usSessionType = ${UserSession.TYPE_UPSTREAM})
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
                 JOIN Chat
                      ON ChangeLog.chTableId = ${Chat.TABLE_ID}
                         AND ChangeLog.chEntityPk = Chat.chatUid
                 JOIN UserSession ON 
                      ((UserSession.usPersonUid IN 
                           (SELECT ChatMember.chatMemberPersonUid 
                              FROM ChatMember 
                             WHERE ChatMember.chatMemberChatUid = Chat.chatUid))
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



    /**
     * The logic in the query uses a Union. The feature expected includes displaying a list of 
     * existing chats on the chat list screen. It also has to include all the people we havent 
     * initiated chats with when we search by person.
     * The first union is responsible for getting all the data relevant to existing chat for the
     * logged in user, along with the latest message and any direct person if the chat is not a group
     * chat.
     * The other union is simply a query to all people.
     */
    @Query("""
        SELECT Chat.*,
               Message.messageText AS latestMessage,
               Message.messageTimestamp AS latestMessageTimestamp,
               op.personUid AS otherPersonUid,
               op.firstNames AS otherPersonFirstNames,
               op.lastName AS otherPersonLastName,
               (
				SELECT COUNT(*) 
				  FROM Message 
				 WHERE Message.messageTableId = ${Chat.TABLE_ID} 
				   AND Message.messageEntityUid = Chat.chatUid 
				   AND Message.messageSenderPersonUid != :personUid
				   AND Message.messageTimestamp > coalesce((
						SELECT MessageRead.messageReadLct FROM MessageRead 
						WHERE MessageRead.messageReadPersonUid = :personUid
						AND MessageRead.messageReadMessageUid = Message.messageUid 
				      ), 0)
					
				
			   ) AS unreadMessageCount,
        
               (SELECT COUNT(*)
                  FROM ChatMember mm
                  WHERE mm.chatMemberChatUid = Chat.chatUid ) AS numMembers
          FROM ChatMember
               LEFT JOIN Chat 
                    ON Chat.chatUid = ChatMember.chatMemberChatUid
               LEFT JOIN Message 
                    ON Message.messageUid =
                        (SELECT messageUid
                           FROM Message
                          WHERE messageEntityUid = Chat.chatUid
                            AND messageTableId = ${Chat.TABLE_ID}
                       ORDER BY messageTimestamp DESC
                          LIMIT 1)
               LEFT JOIN Person op 
                    ON op.personUid =
                       (SELECT pp.personUid
                          FROM ChatMember cm
                               LEFT JOIN Person pp 
                                    ON pp.personUid = cm.chatMemberPersonUid
                         WHERE cm.chatMemberChatUid = Chat.chatUid
                           AND cm.chatMemberPersonUid != :personUid
                           AND cm.chatMemberLeftDate = ${Long.MAX_VALUE}
                         LIMIT 1)
         WHERE ChatMember.chatMemberPersonUid = :personUid
           AND ChatMember.chatMemberLeftDate = ${Long.MAX_VALUE}
           AND Chat.chatUid != 0 
           AND op.firstNames||' '||op.lastName LIKE :searchBit 
        -- When in search mode we need to add all Persons who match the search to the list, even if
        -- no chat has started
        UNION
        SELECT Chat.*,
               '' AS latestMessage,
                    0 AS latestMessageTimestamp,
                    Person.personUid AS otherPersonUid,
                    Person.firstNames AS otherPersonFirstNames,
                    Person.lastName AS otherPersonLastName,
                    0 AS unreadMessageCount,
                    0 AS numMembers
                              
          FROM PersonGroupMember
             ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_PERSON_SELECT}
                    ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
		  
               LEFT JOIN Chat
                    ON Chat.chatUid = 0
         WHERE :searchBit != '%'
           AND PersonGroupMember.groupMemberPersonUid = :personUid
           AND Person.personUid != :personUid
        
           AND Person.personUid NOT IN
			   (
				SELECT chatpeople.personUid 
				  FROM ChatMember cmm
					   LEFT JOIN Chat cc 
							  ON cc.chatUid = cmm.chatMemberChatUid 
			   
				 LEFT JOIN Person chatpeople 
                    ON chatpeople.personUid =
                       (SELECT chatpeopleother.personUid
                          FROM ChatMember cm
                               LEFT JOIN Person chatpeopleother 
                                    ON chatpeopleother.personUid = cm.chatMemberPersonUid
                         WHERE cm.chatMemberChatUid = cc.chatUid
                           AND cm.chatMemberPersonUid != :personUid
                         LIMIT 1)
						 
				 WHERE cc.chatUid != 0 
				   AND cmm.chatMemberPersonUid = :personUid
				 )
                            
           AND Person.firstNames||' '||Person.lastName LIKE :searchBit 
         ORDER BY latestMessageTimestamp DESC
    """)
    abstract fun findAllChatsForUser(searchBit: String, personUid: Long)
        : DataSourceFactory<Int, ChatWithLatestMessageAndCount>


    @Query("""
        SELECT CASE
                   WHEN Chat.chatGroup THEN Chat.chatTitle
                   ELSE Person.firstNames||' '||Person.lastName
               END AS title
        FROM Chat
        LEFT JOIN Person 
        ON CAST(Chat.chatGroup AS INTEGER) = 0
           AND Person.personUid =
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



    @Query("""
        SELECT Chat.*
          FROM ChatMember
          LEFT JOIN Chat ON Chat.chatUid = ChatMember.chatMemberChatUid
         WHERE ChatMember.chatMemberPersonUid = :otherPersonUid
           AND CAST(Chat.chatGroup AS INTEGER) = 0
           AND Chat.chatUid IN 
               (
                SELECT ChatMember.chatMemberChatUid
                  FROM ChatMember
                 WHERE ChatMember.chatMemberChatUid = Chat.chatUid
                   AND ChatMember.chatMemberPersonUid = :loggedInPersonUid 
               ) 
           AND :otherPersonUid != :loggedInPersonUid
    """)
    abstract suspend fun getChatByOtherPerson(otherPersonUid: Long, loggedInPersonUid: Long): Chat?

}