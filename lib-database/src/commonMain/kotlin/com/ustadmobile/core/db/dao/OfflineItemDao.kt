package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.composites.OfflineItemAndState
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.lib.db.entities.OfflineItem
import kotlinx.coroutines.flow.Flow


@DoorDao
@Repository
expect abstract class OfflineItemDao {

    @Insert
    abstract suspend fun insertAsync(item: OfflineItem)

    /**
     * Get the OfflineItemAndState for a given ContentEntry
     */
    @Query("""
        SELECT OfflineItem.*,
               TransferJob.*,
               ${TransferJobDaoCommon.SELECT_TRANSFER_JOB_TOTALS_SQL},
               /* NEXT Here - check completedjob time created should be AFTER offline item last modified time */
               (SELECT EXISTS(
                       SELECT CompletedJob.tjUid
                         FROM TransferJob CompletedJob
                        WHERE CompletedJob.tjTableId = ${ContentEntryVersion.TABLE_ID}
                          AND CompletedJob.tjEntityUid IN  
                              ${TransferJobDaoCommon.SELECT_CONTENT_ENTRY_VERSION_UIDS_FOR_CONTENT_ENTRY_UID_SQL}
                          AND CompletedJob.tjStatus = ${TransferJobItemStatus.STATUS_COMPLETE_INT}    
                        LIMIT 1      
               )) AS readyForOffline
          FROM OfflineItem
               LEFT JOIN TransferJob 
                         ON TransferJob.tjUid = 
                         (SELECT TransferJob.tjUid
                            FROM TransferJob
                           WHERE TransferJob.tjTableId = ${ContentEntryVersion.TABLE_ID}
                             AND TransferJob.tjEntityUid IN  
                                 ${TransferJobDaoCommon.SELECT_CONTENT_ENTRY_VERSION_UIDS_FOR_CONTENT_ENTRY_UID_SQL}
                             AND TransferJob.tjStatus < ${TransferJobItemStatus.STATUS_COMPLETE_INT}
                        ORDER BY TransferJob.tjTimeCreated DESC     
                           LIMIT 1)
         WHERE OfflineItem.oiContentEntryUid = :contentEntryUid
           AND CAST(OfflineItem.oiActive AS INTEGER) = 1
      ORDER BY OfflineItem.oiLct
         LIMIT 1     
    """)
    abstract fun findByContentEntryUid(contentEntryUid: Long): Flow<OfflineItemAndState?>


}