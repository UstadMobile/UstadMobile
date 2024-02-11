package com.ustadmobile.core.db.dao

import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.DeletedItem

@DoorDao
@Repository
expect abstract class DeletedItemDao {

    @Query("""
        INSERT INTO DeletedItem(delItemName, delItemIconUri, delItemLastModTime, delItemTimeDeleted, delItemEntityTable, delItemEntityUid, delItemDeletedByPersonUid, delItemStatus)
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
               ${DeletedItem.STATUS_PENDING} AS delItemStatus
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

}