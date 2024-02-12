package com.ustadmobile.core.db.dao

import androidx.room.Query
import app.cash.paging.PagingSource
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.HttpServerFunctionCall
import com.ustadmobile.door.annotation.HttpServerFunctionParam
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.DeletedItem

@DoorDao
@Repository
expect abstract class DeletedItemDao {

    @Query("""
        INSERT INTO DeletedItem(delItemName, delItemIconUri, delItemLastModTime, delItemTimeDeleted, delItemEntityTable, delItemEntityUid, delItemDeletedByPersonUid, delItemStatus, delItemIsFolder)
        SELECT (SELECT ContentEntry.title
                  FROM ContentEntry
                 WHERE ContentEntry.contentEntryUid = 
                       (SELECT ContentEntryParentChildJoin.cepcjChildContentEntryUid
                          FROM ContentEntryParentChildJoin
                         WHERE ContentEntryParentChildJoin.cepcjUid = :cepcjUid)) AS delItemName,
               NULL as delItemIconUri,
               :time AS delItemLastModTime,
               :time AS delItemTimeDeleted,
               ${ContentEntryParentChildJoin.TABLE_ID} AS delItemEntityTable,
               :cepcjUid AS delItemEntityUid,
               :deletedByPersonUid AS delItemDeletedByPersonUid,
               ${DeletedItem.STATUS_PENDING} AS delItemStatus,
               (SELECT NOT ContentEntry.leaf
                  FROM ContentEntry
                 WHERE ContentEntry.contentEntryUid = 
                       (SELECT ContentEntryParentChildJoin.cepcjChildContentEntryUid
                          FROM ContentEntryParentChildJoin
                         WHERE ContentEntryParentChildJoin.cepcjUid = :cepcjUid)) AS delItemIsFolder
    """)
    abstract suspend fun insertDeletedItemForContentEntryParentChildJoin(
        cepcjUid: Long,
        time: Long,
        deletedByPersonUid: Long,
    )


    @Query("""
        SELECT DeletedItem.*
          FROM DeletedItem
         WHERE DeletedItem.delItemEntityTable = :tableId
           AND DeletedItem.delItemEntityUid = :entityUid
    """)
    abstract suspend fun findByTableIdAndEntityUid(
        tableId: Int,
        entityUid: Long,
    ): List<DeletedItem>


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findDeletedItemsForUser",
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "includeActionedItems",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true"
                    )
                )
            )
        )
    )
    @Query("""
        SELECT DeletedItem.*
          FROM DeletedItem
         WHERE (  (CAST(:includeActionedItems AS INTEGER) = 1)
                OR DeletedItem.delItemStatus = ${DeletedItem.STATUS_PENDING})
           AND DeletedItem.delItemDeletedByPersonUid = :personUid       
      ORDER BY DeletedItem.delItemTimeDeleted DESC            
    """)
    abstract fun findDeletedItemsForUser(
        personUid: Long,
        includeActionedItems: Boolean,
    ): PagingSource<Int, DeletedItem>

    @Query("""
        UPDATE DeletedItem
           SET delItemStatus = :newStatus,
               delItemLastModTime = :updateTime
         WHERE delItemUid IN (:uidList)
    """)
    abstract suspend fun updateStatusByUids(
        uidList: List<Long>,
        newStatus: Int,
        updateTime: Long,
    )

}