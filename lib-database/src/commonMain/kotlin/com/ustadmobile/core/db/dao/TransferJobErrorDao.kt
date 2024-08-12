package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.lib.db.entities.TransferJobError

@DoorDao
expect abstract class TransferJobErrorDao {

    @Insert
    abstract suspend fun insertAsync(error: TransferJobError)

    @Query("""
        SELECT TransferJobError.*
          FROM TransferJobError
         WHERE TransferJobError.tjeTjUid = :jobUid
    """)
    abstract suspend fun findByJobId(jobUid: Int): List<TransferJobError>

    @Query("""
        UPDATE TransferJobError
           SET tjeDismissed = :dismissed
         WHERE TransferJobError.tjeTjUid = :jobUid  
    """)
    abstract suspend fun dismissErrorByJobId(jobUid: Int, dismissed: Boolean)

}