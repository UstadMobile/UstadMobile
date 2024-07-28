package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.core.db.dao.TransferJobDaoCommon.SELECT_CONTENT_ENTRY_VERSION_UIDS_FOR_CONTENT_ENTRY_UID_SQL
import com.ustadmobile.core.db.dao.TransferJobDaoCommon.SELECT_TRANSFER_JOB_TOTALS_SQL
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
        SELECT COALESCE(
            (SELECT TransferJob.tjStatus
               FROM TransferJob
              WHERE tjUid = :jobUid), 0)
    """)
    abstract suspend fun getJobStatus(jobUid: Int): Int

    @Query("""
        UPDATE TransferJob
           SET tjStatus = ${TransferJobItemStatus.STATUS_COMPLETE_INT}
         WHERE tjUid = :jobUid
          AND NOT EXISTS(
              SELECT TransferJobItem.tjiUid
                FROM TransferJobItem
               WHERE TransferJobItem.tjiTjUid = :jobUid
                 AND TransferJobItem.tjiStatus != ${TransferJobItemStatus.STATUS_COMPLETE_INT}) 
    """)
    abstract suspend fun updateStatusIfComplete(jobUid: Int): Int

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
               $SELECT_TRANSFER_JOB_TOTALS_SQL,
               TransferJobError.tjeErrorStr AS latestErrorStr
          FROM TransferJob
               LEFT JOIN TransferJobError
                         ON TransferJobError.tjeId = 
                            (SELECT TransferJobError.tjeId
                               FROM TransferJobError
                              WHERE TransferJob.tjStatus = ${TransferJobItemStatus.STATUS_FAILED}
                                AND TransferJobError.tjeTjUid = TransferJob.tjUid
                           ORDER BY TransferJobError.tjeDismissed DESC 
                              LIMIT 1)
         WHERE TransferJob.tjTableId = ${ContentEntryVersion.TABLE_ID}
           AND TransferJob.tjEntityUid IN 
               $SELECT_CONTENT_ENTRY_VERSION_UIDS_FOR_CONTENT_ENTRY_UID_SQL 
           AND (   TransferJob.tjStatus < ${TransferJobItemStatus.STATUS_COMPLETE_INT}
                OR (TransferJobError.tjeErrorStr IS NOT NULL AND NOT TransferJobError.tjeDismissed))
           AND TransferJob.tjType = :jobType   
    """)
    abstract fun findByContentEntryUidWithTotalsAsFlow(
        contentEntryUid: Long,
        jobType: Int,
    ): Flow<List<TransferJobAndTotals>>


    @Query("""
        SELECT COALESCE(
               (SELECT TransferJob.tjOiUid
                  FROM TransferJob
                 WHERE TransferJob.tjUid = :jobUid), 0)
    """)
    abstract suspend fun findOfflineItemUidForTransferJobUid(
        jobUid: Int
    ): Long
}