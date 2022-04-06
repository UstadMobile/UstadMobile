package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.SyncNode
import com.ustadmobile.door.annotation.*
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
        FROM Message
       WHERE Message.messageLct != COALESCE(
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
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)


    @Query("""
         REPLACE INTO MessageReplicate(messagePk, messageDestination)
          SELECT DISTINCT Message.messageUid AS messageUid,
                 UserSession.usClientNodeId AS messageDestination
            FROM ChangeLog
                 JOIN Message
                     ON ChangeLog.chTableId = ${Message.TABLE_ID}
                        AND ChangeLog.chEntityPk = Message.messageUid
                 JOIN UserSession ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
           WHERE UserSession.usClientNodeId != (
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


    @Insert
    abstract suspend fun updateAsync(entity: Message)
}