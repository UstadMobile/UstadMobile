package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*

@Dao
abstract class ContentJobItemDao {

    @Query("""
        WITH ConnectivityStateCte(state) AS 
             (SELECT COALESCE(
                     (SELECT connectivityState 
                        FROM ConnectivityStatus 
                       LIMIT 1), 0))
                       
        SELECT ContentJobItem.*, ContentJob.*
          FROM ContentJobItem
               JOIN ContentJob
               ON ContentJobItem.cjiJobUid = ContentJob.cjUid
         WHERE ContentJobItem.cjiJobUid = :contentJobUid
           AND (ContentJobItem.cjiStatus = ${JobStatus.QUEUED} OR 
                ContentJobItem.cjiStatus = ${JobStatus.WAITING_FOR_CONNECTION})
           AND (
                NOT cjiConnectivityNeeded 
                OR ((SELECT state FROM ConnectivityStateCte) = ${ConnectivityStatus.STATE_UNMETERED}) 
                OR (cjIsMeteredAllowed 
                    AND (SELECT state FROM ConnectivityStateCte) = ${ConnectivityStatus.STATE_METERED})
                )
         LIMIT :limit
    """)
    abstract suspend fun findNextItemsInQueue(contentJobUid: Long, limit: Int) : List<ContentJobItemAndContentJob>


    @Query("""
        SELECT cjiRecursiveProgress AS progress, 
               cjiRecursiveTotal AS total, 
               cjNotificationTitle as progressTitle,
               ContentJobItem.cjiUid
          FROM ContentJobItem
          JOIN ContentJob
            ON ContentJob.cjUid = ContentJobItem.cjiJobUid
         WHERE cjiContentEntryUid = :contentEntryUid
           AND cjiRecursiveStatus >= ${JobStatus.QUEUED}
           AND cjiRecursiveStatus <= ${JobStatus.RUNNING_MAX}
      ORDER BY cjiStartTime DESC
    """)
    abstract suspend fun findActiveContentJobItems(contentEntryUid: Long): List<ContentJobItemProgress>


    @Insert
    abstract suspend fun insertJobItem(jobItem: ContentJobItem) : Long

    @Insert
    abstract suspend fun insertJobItems(jobItems: List<ContentJobItem>)


    @Query("""
        UPDATE ContentJobItem 
           SET cjiStatus = :status
         WHERE cjiUid= :cjiUid  
    """)
    abstract suspend fun updateItemStatus(cjiUid: Long, status: Int)

    @Query("""
        SELECT NOT EXISTS(
               SELECT cjiUid 
                 FROM ContentJobItem
                WHERE cjiJobUid = :jobUid
                  AND cjiStatus < ${JobStatus.COMPLETE_MIN}) 
    """)
    abstract suspend fun isJobDone(jobUid: Long): Boolean

    @Query("""
        SELECT * 
          FROM ContentJobItem
         WHERE cjiJobUid = :jobUid 
           AND cjiParentCjiUid = 0 
         LIMIT 1
    """)
    abstract fun findRootJobItemByJobId(jobUid: Long): ContentJobItem?

    @Query("""
        UPDATE ContentJobItem
           SET cjiItemProgress = :cjiProgress,
               cjiItemTotal = :cjiTotal
         WHERE cjiUid = :cjiUid     
    """)
    abstract suspend fun updateItemProgress(cjiUid: Long, cjiProgress: Long, cjiTotal: Long)


    @Query("""
        UPDATE ContentJobItem
           SET cjiConnectivityNeeded = :connectivityNeeded
         WHERE cjiUid = :contentJobItemId     
    """)
    abstract suspend fun updateConnectivityNeeded(contentJobItemId: Long, connectivityNeeded: Boolean)


    @Query("""
        UPDATE ContentJobItem
           SET cjiContainerProcessed = :cjiContainerProcessed
         WHERE cjiUid = :contentJobItemId   
    """)
    abstract suspend fun updateContainerProcessed(contentJobItemId: Long, cjiContainerProcessed: Boolean)

    @Transaction
    open suspend fun commitProgressUpdates(updates: List<ContentJobItemProgressUpdate>) {
        updates.forEach {
            updateItemProgress(it.cjiUid, it.cjiItemProgress, it.cjiItemTotal)
        }
    }


    @Query("""
        UPDATE ContentJobItem
           SET cjiStatus = :status,
               cjiAttemptCount = :attemptCount
         WHERE cjiUid = :cjiUid      
    """)
    abstract suspend fun updateJobItemAttemptCountAndStatus(cjiUid: Long, attemptCount: Int, status: Int)


    @Query("""
        UPDATE ContentJobItem
           SET cjiStartTime = :startTime
         WHERE cjiUid = :cjiUid      
    """)
    abstract suspend fun updateStartTimeForJob(cjiUid: Long, startTime: Long)

    @Query("""
        UPDATE ContentJobItem
           SET cjiFinishTime = :finishTime
         WHERE cjiUid = :cjiUid      
    """)
    abstract suspend fun updateFinishTimeForJob(cjiUid: Long, finishTime: Long)

    @Query("""
        UPDATE ContentJobITem
           SET cjiContentEntryUid = :contentEntryUid
         WHERE cjiUid = :cjiUid  
    """)
    abstract suspend fun updateContentEntryUid(cjiUid: Long, contentEntryUid: Long)

    @Query("""
        UPDATE ContentJobItem
           SET cjiContainerUid = :containerUid
         WHERE cjiUid = :cjiUid  
    """)
    abstract suspend fun updateContentJobItemContainer(cjiUid: Long, containerUid: Long)

    @Query("""
        SELECT * 
          FROM ContentJobItem
    """)
    abstract suspend fun findAll(): List<ContentJobItem>

    @Query("""
        SELECT ContentJobItem.*
          FROM ContentJobItem
         WHERE cjiUid = :cjiUid 
    """)
    abstract suspend fun findByUidAsync(cjiUid: Long): ContentJobItem?


    @Query("""
        SELECT COALESCE(
               (SELECT ContentJobItem.cjiJobUid
                  FROM ContentJobItem
                 WHERE cjiContentEntryUid = :contentEntryUid
                   AND cjiStatus BETWEEN ${JobStatus.QUEUED} AND ${JobStatus.RUNNING_MAX}
              ORDER BY cjiFinishTime DESC), 0)
    """)
    abstract suspend fun getActiveContentJobIdByContentEntryUid(contentEntryUid: Long): Long


    @Query("""
        UPDATE ContentJobItem
           SET cjiUploadSessionUid = :uploadSessionUuid
         WHERE cjiUid = :cjiUid  
    """)
    abstract suspend fun updateUploadSessionUuid(cjiUid: Long, uploadSessionUuid: String)


    @Query("""
        SELECT * 
          FROM ContentJobItem
         WHERE cjiJobUid = :jobId 
    """)
    abstract fun findAllByJobId(jobId: Long): List<ContentJobItem>

    @Query("""
        SELECT *
          FROM ContentJobItem
         WHERE cjiUid = :uid   
    """)
    abstract fun getJobItemByUidLive(uid: Long): DoorLiveData<ContentJobItem?>

    @Query("""
        SELECT cjiContainerUid
          FROM ContentJobItem
         WHERE cjiUid = :uid 
    """)
    abstract suspend fun getContainerUidByJobItemUid(uid: Long): Long

    @Query("""
        UPDATE ContentJobItem
           SET cjiStatus = :newStatus
         WHERE cjiJobUid = :jobUid
           AND cjiStatus != :newStatus
    """)
    abstract suspend fun updateAllStatusesByJobUid(jobUid: Long, newStatus: Int)

    @Query("""
        SELECT ContentJobItem.cjiContainerUid
          FROM ContentJobItem
         WHERE cjiJobUid = :jobUid 
    """)
    abstract suspend fun findAllContainersByJobUid(jobUid: Long): List<Long>


}