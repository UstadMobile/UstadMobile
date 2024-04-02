package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.lib.db.composites.ContentEntryImportJobProgress
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.flow.Flow

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



    @Query("""
        SELECT ContentEntryImportJob.cjiUid,
               ContentEntryImportJob.cjiItemProgress,
               ContentEntryImportJob.cjiItemTotal,
               ContentEntryImportJob.cjiStatus
          FROM ContentEntryImportJob
         WHERE ContentEntryImportJob.cjiContentEntryUid = :contentEntryUid
           AND ContentEntryImportJob.cjiStatus BETWEEN ${JobStatus.QUEUED} AND ${JobStatus.RUNNING_MAX}
    """)
    abstract fun findInProgressJobsByContentEntryUid(
        contentEntryUid: Long,
    ): Flow<List<ContentEntryImportJobProgress>>

}