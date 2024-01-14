package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.lib.db.composites.TransferJobAndTotals
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.lib.db.entities.TransferJob
import kotlinx.coroutines.flow.Flow

@DoorDao
expect abstract class TransferJobDao {

    @Insert
    abstract suspend fun insert(job: TransferJob): Long

    @Query("""
        SELECT TransferJob.*
          FROM TransferJob
         WHERE TransferJob.tjUid = :jobUid
    """)
    abstract suspend fun findByUid(jobUid: Int): TransferJob?

    @Query("""
        SELECT TransferJob.*
          FROM TransferJob
         WHERE TransferJob.tjUid = :jobUid
    """)
    abstract fun findByUidAsFlow(jobUid: Int): Flow<TransferJob?>

    @Query("""
        UPDATE TransferJob
           SET tjStatus = :status
         WHERE tjUid = :jobUid  
    """)
    abstract suspend fun updateStatus(jobUid: Int, status: Int)

    @Query("""
        UPDATE TransferJob
           SET tjStatus = ${TransferJobItemStatus.STATUS_COMPLETE_INT}
         WHERE tjUid = :jobUid
          AND NOT EXISTS(
              SELECT TransferJobItem.tjiUid
                FROM TransferJobItem
               WHERE TransferJobItem.tjiStatus != ${TransferJobItemStatus.STATUS_COMPLETE_INT}) 
    """)
    abstract suspend fun updateStatusIfComplete(jobUid: Int)

    @Query("""
        SELECT TransferJob.*
          FROM TransferJob
         WHERE EXISTS(
               SELECT TransferJobItem.tjiUid
                 FROM TransferJobItem
                WHERE TransferJobItem.tjiTjUid = TransferJob.tjUid
                  AND TransferJobItem.tjiTableId = :tableId
                  AND TransferJobItem.tjiEntityUid = :entityUid) 
    """)
    abstract suspend fun findJobByEntityAndTableUid(
        tableId: Int,
        entityUid: Long
    ): List<TransferJob>


    @Query("""
        SELECT TransferJob.*,
                (SELECT SUM(TransferJobItem.tjTotalSize)
                   FROM TransferJobItem
                  WHERE TransferJobItem.tjiTjUid =  TransferJob.tjUid) AS totalSize,
                (SELECT SUM(TransferJobItem.tjTransferred)
                   FROM TransferJobItem
                  WHERE TransferJobItem.tjiTjUid =  TransferJob.tjUid) AS transferred  
          FROM TransferJob
         WHERE EXISTS(
               SELECT TransferJobItem.tjiUid
                 FROM TransferJobItem
                WHERE TransferJobItem.tjiTjUid = TransferJob.tjUid
                  AND TransferJobItem.tjiTableId = ${ContentEntryVersion.TABLE_ID}
                  AND TransferJobItem.tjiEntityUid IN
                      (SELECT ContentEntryVersion.cevUid
                         FROM ContentEntryVersion
                        WHERE ContentEntryVersion.cevContentEntryUid = :contentEntryUid)
                LIMIT 1)
           AND TransferJob.tjStatus < ${TransferJobItemStatus.STATUS_COMPLETE_INT}     
    """)
    abstract fun findByContentEntryUidWithTotalsAsFlow(
        contentEntryUid: Long
    ): Flow<List<TransferJobAndTotals>>

}