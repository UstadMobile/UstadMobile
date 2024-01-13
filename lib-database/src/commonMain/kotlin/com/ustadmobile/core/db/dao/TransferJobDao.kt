package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
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

}