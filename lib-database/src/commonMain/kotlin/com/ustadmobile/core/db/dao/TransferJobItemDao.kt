package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import com.ustadmobile.lib.db.composites.TransferJobItemStatus.Companion.STATUS_COMPLETE_INT
import com.ustadmobile.lib.db.entities.TransferJobItem

@DoorDao
expect abstract class TransferJobItemDao {

    @Insert
    abstract suspend fun insertList(items: List<TransferJobItem>)

    @Insert
    abstract suspend fun insert(item: TransferJobItem): Long

    @Query("""
        SELECT TransferJobItem.*
          FROM TransferJobItem
         WHERE TransferJobItem.tjiTjUid = :jobUid
    """)
    abstract suspend fun findByJobUid(jobUid: Int): List<TransferJobItem>


    @Query("""
        SELECT TransferJobItem.*
          FROM TransferJobItem
         WHERE TransferJobItem.tjiTjUid = :jobUid
           AND TransferJobItem.tjiStatus < ${TransferJobItemStatus.STATUS_COMPLETE_INT}
    """)
    abstract suspend fun findPendingByJobUid(jobUid: Int): List<TransferJobItem>

    @Query("""
        UPDATE TransferJobItem
           SET tjTransferred = :transferred
         WHERE tjiUid = :jobItemUid
    """)
    abstract suspend fun updateTransferredProgress(
        jobItemUid: Int,
        transferred: Long,
    )

    @Query("""
        UPDATE TransferJobItem
           SET tjiStatus = :status
         WHERE tjiUid = :jobItemUid  
    """)
    abstract suspend fun updateStatus(
        jobItemUid: Int,
        status: Int,
    )


    /**
     * When a TransferJobItem (e.g. upload) is completed for a given tableuid and entity uid,
     * insert into the OutgoingReplication table so that the update can take effect on the upstream
     * server.
     *
     * It is possible that there is more than one TransferJobItem relating to a single entity -
     * e.g. the upload of a picture and thumbnail. In this case the OutgoingReplication will only
     * be inserted when there is no further pending (incomplete) transferJobItem for the same entity.
     */
    @Query("""
        INSERT INTO OutgoingReplication(destNodeId, orTableId, orPk1, orPk2)
        SELECT :destNodeId AS destNodeId, 
              TransferJobItem.tjiTableId AS orTableId,
              TransferJobItem.tjiEntityUid AS orPk1,
              0 AS orPk2
        FROM TransferJobItem
       WHERE TransferJobItem.tjiUid = :transferJobItemUid
         AND TransferJobItem.tjiTableId != 0
         AND TransferJobItem.tjiStatus = $STATUS_COMPLETE_INT
         AND NOT EXISTS(
             SELECT OtherJob.tjiUid
               FROM TransferJobItem OtherJob
              WHERE OtherJob.tjiTableId = TransferJobItem.tjiTableId
                AND OtherJob.tjiEntityUid = TransferJobItem.tjiEntityUid
                AND OtherJob.tjiEntityEtag = TransferJobItem.tjiEntityEtag
                AND OtherJob.tjiStatus != $STATUS_COMPLETE_INT)
    """)
    abstract suspend fun insertOutgoingReplicationForTransferJobItemIfDone(
        destNodeId: Long,
        transferJobItemUid: Int,
    )

    @Query("""
        UPDATE TransferJobItem
           SET tjiStatus = :status
         WHERE tjiTjUid = :jobUid
           AND tjiStatus != $STATUS_COMPLETE_INT 
    """)
    abstract suspend fun updateStatusIfNotCompleteForAllInJob(
        jobUid: Int,
        status: Int
    )

    @Query("""
       SELECT COUNT(*)
         FROM TransferJobItem
        WHERE TransferJobItem.tjiTjUid = :jobUid
          AND TransferjobItem.tjiStatus != ${TransferJobItemStatus.STATUS_COMPLETE_INT}
    """)
    abstract suspend fun findNumberJobItemsNotComplete(
        jobUid: Int,
    ): Int

}