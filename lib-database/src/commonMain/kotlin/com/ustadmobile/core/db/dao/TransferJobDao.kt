package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.lib.db.entities.TransferJob

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

}