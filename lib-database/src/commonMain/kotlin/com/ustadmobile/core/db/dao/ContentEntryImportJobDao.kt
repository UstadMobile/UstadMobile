package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.core.db.dao.ContentEntryImportJobDaoCommon.FIND_IN_PROGRESS_JOBS_BY_CONTENT_ENTRY_UID
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
           SET cjiStatus = :status,
               cjiError = :error
         WHERE cjiUid= :cjiUid  
    """)
    abstract suspend fun updateItemStatusAndError(cjiUid: Long, status: Int, error: String?)


    @Query("""
        UPDATE ContentEntryImportJob
           SET cjiErrorDismissed = :dismissed
         WHERE cjiUid = :cjiUid  
    """)
    abstract suspend fun updateErrorDismissed(cjiUid: Long, dismissed: Boolean)

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
        SELECT COALESCE(
               (SELECT ContentEntryImportJob.cjiOwnerPersonUid
                  FROM ContentEntryImportJob
                 WHERE ContentEntryImportJob.cjiUid = :cjiUid), 0)
    """)
    abstract suspend fun findOwnerByUidAsync(cjiUid: Long): Long


    @Query(FIND_IN_PROGRESS_JOBS_BY_CONTENT_ENTRY_UID)
    abstract fun findInProgressJobsByContentEntryUid(
        contentEntryUid: Long,
    ): Flow<List<ContentEntryImportJobProgress>>


    @Query(FIND_IN_PROGRESS_JOBS_BY_CONTENT_ENTRY_UID)
    abstract suspend fun findInProgressJobsByContentEntryUidAsync(
        contentEntryUid: Long,
    ): List<ContentEntryImportJobProgress>


}