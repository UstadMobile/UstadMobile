package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.ContentJobItem.Companion.ACCEPT_METERED
import com.ustadmobile.lib.db.entities.ContentJobItem.Companion.ACCEPT_NONE
import com.ustadmobile.lib.db.entities.ContentJobItem.Companion.ACCEPT_UNMETERED
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import com.ustadmobile.lib.db.entities.ContentJobItemProgressUpdate

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
           AND ContentJobItem.cjiStatus BETWEEN ${JobStatus.QUEUED} AND ${JobStatus.COMPLETE_MIN}
           AND (
                ((cjiConnectivityAcceptable & $ACCEPT_NONE) = $ACCEPT_NONE)
                OR (((cjiConnectivityAcceptable & $ACCEPT_UNMETERED) = $ACCEPT_UNMETERED) 
                     AND (SELECT state FROM ConnectivityStateCte) = ${ConnectivityStatus.STATE_UNMETERED})
                OR (((cjiConnectivityAcceptable & $ACCEPT_METERED) = $ACCEPT_METERED) 
                     AND (SELECT state FROM ConnectivityStateCte) = ${ConnectivityStatus.STATE_METERED})
                )
         LIMIT :limit
    """)
    abstract suspend fun findNextItemsInQueue(contentJobUid: Long, limit: Int) : List<ContentJobItemAndContentJob>

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
        UPDATE ContentJobItem
           SET cjiItemProgress = :cjiProgress,
               cjiItemTotal = :cjiTotal
         WHERE cjiUid = :cjiUid     
    """)
    abstract suspend fun updateItemProgress(cjiUid: Long, cjiProgress: Long, cjiTotal: Long)

    @Transaction
    open suspend fun commitProgressUpdates(updates: List<ContentJobItemProgressUpdate>) {
        updates.forEach {
            updateItemProgress(it.cjiUid, it.cjiProgress, it.cjiTotal)
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
        UPDATE ContentJobITem
           SET cjiContentEntryUid = :contentEntryUid
         WHERE cjiUid = :cjiUid  
    """)
    abstract suspend fun updateContentEntryUid(cjiUid: Long, contentEntryUid: Long)

    @Query("""
        SELECT * 
          FROM ContentJobItem
    """)
    abstract suspend fun findAll(): List<ContentJobItem>

}