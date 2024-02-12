package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.Query
import app.cash.paging.PagingSource
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.composites.MessageAndOtherPerson
import com.ustadmobile.lib.db.entities.*

@DoorDao
@Repository
expect abstract class MessageDao {

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""
        SELECT Message.*
          FROM Message
         WHERE (Message.messageSenderPersonUid = :accountPersonUid
                AND Message.messageToPersonUid = :otherPersonUid)
            OR (Message.messageSenderPersonUid = :otherPersonUid
                AND Message.messageToPersonUid = :accountPersonUid) 
      ORDER BY Message.messageTimestamp DESC          
    """)
    abstract fun messagesFromOtherUserAsPagingSource(
        accountPersonUid: Long,
        otherPersonUid: Long,
    ): PagingSource<Int, Message>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""
        SELECT Person.*, LatestMessage.*, PersonPicture.*
          FROM Person
               JOIN Message LatestMessage
                    ON LatestMessage.messageUid = 
                       (SELECT Message.messageUid
                          FROM Message
                         WHERE (Message.messageSenderPersonUid = :accountPersonUid
                                AND Message.messageToPersonUid = Person.personUid)
                            OR (Message.messageSenderPersonUid = Person.personUid
                                AND Message.messageToPersonUid = :accountPersonUid)
                       ORDER BY Message.messageTimestamp DESC
                          LIMIT 1)
                          
                LEFT JOIN PersonPicture
                          ON PersonPicture.personPictureUid = Person.personUid
         WHERE :searchQuery = '%' 
               OR (Person.firstNames || ' ' || Person.lastName) LIKE :searchQuery
      ORDER BY LatestMessage.messageTimestamp DESC
    """)
    abstract fun conversationsForUserAsPagingSource(
        searchQuery: String,
        accountPersonUid: Long
    ): PagingSource<Int, MessageAndOtherPerson>

    @Insert
    abstract suspend fun insert(message: Message)


}