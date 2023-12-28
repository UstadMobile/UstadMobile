package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.lib.db.entities.TransferJobItem

@DoorDao
expect abstract class TransferJobItemDao {

    @Insert
    abstract suspend fun insertList(items: List<TransferJobItem>)

    @Query("""
        SELECT TransferJobItem.*
          FROM TransferJobItem
         WHERE TransferJobItem.tjiTjUid = :jobUid
    """)
    abstract suspend fun findByJobUid(jobUid: Int): List<TransferJobItem>


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

}