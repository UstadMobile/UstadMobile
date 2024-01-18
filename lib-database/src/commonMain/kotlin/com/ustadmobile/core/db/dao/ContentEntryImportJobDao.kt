package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.lib.db.entities.*

@DoorDao
expect abstract class ContentEntryImportJobDao {

    @Insert
    abstract suspend fun insertJobItem(jobItem: ContentEntryImportJob) : Long

    @Query("""
        UPDATE ContentEntryImportJob 
           SET cjiStatus = :status
         WHERE cjiUid= :cjiUid  
    """)
    abstract suspend fun updateItemStatus(cjiUid: Long, status: Int)

    @Query("""
        UPDATE ContentEntryImportJob
           SET cjiItemProgress = :cjiProgress,
               cjiItemTotal = :cjiTotal
         WHERE cjiUid = :cjiUid     
    """)
    abstract suspend fun updateItemProgress(cjiUid: Long, cjiProgress: Long, cjiTotal: Long)

    @Query("""
        SELECT ContentEntryImportJob.*
          FROM ContentEntryImportJob
         WHERE cjiUid = :cjiUid 
    """)
    abstract suspend fun findByUidAsync(cjiUid: Long): ContentEntryImportJob?



}