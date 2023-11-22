package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.Query
import app.cash.paging.PagingSource
import com.ustadmobile.door.SyncNode
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.flow.Flow

@DoorDao
@Repository
expect abstract class MessageDao: BaseDao<Message>{


    @Query("""
       SELECT
              Message.*,
              Person.*,
              MessageRead.*
        FROM Message
        LEFT JOIN Person
          ON Message.messageSenderPersonUid = Person.personUid
        LEFT JOIN MessageRead
          ON MessageRead.messageReadMessageUid = Message.messageUid
             AND MessageRead.messageReadPersonUid = :loggedInPersonUid
       WHERE Message.messageTableId = :tableId
              AND Message.messageEntityUid = :entityUid
    ORDER BY Message.messageTimestamp DESC
    """)
    abstract fun findAllMessagesByChatUid(entityUid: Long, tableId: Int, loggedInPersonUid: Long):
            PagingSource<Int, MessageWithPerson>


    @Query("""
       SELECT
              Message.*,
              Person.*,
              MessageRead.*
        FROM Message
        LEFT JOIN Person
          ON Message.messageSenderPersonUid = Person.personUid
        LEFT JOIN MessageRead
          ON MessageRead.messageReadMessageUid = Message.messageUid
             AND MessageRead.messageReadPersonUid = :loggedInPersonUid
       WHERE Message.messageTableId = :tableId
              AND Message.messageEntityUid = :entityUid
    ORDER BY Message.messageTimestamp DESC
    """)
    abstract fun findAllMessagesByChatUidAsFlow(entityUid: Long, tableId: Int, loggedInPersonUid: Long):
            Flow<List<MessageWithPerson>>


}