package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.core.db.JobStatus.NOT_QUEUED
import com.ustadmobile.core.db.JobStatus.QUEUED
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_METERED
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_UNMETERED
import com.ustadmobile.lib.db.entities.ContainerImportJob

@DoorDao
expect abstract class ContainerImportJobDao : BaseDao<ContainerImportJob> {

    @Query("SELECT * FROM ContainerImportJob WHERE cijSessionId = :sessionId")
    abstract fun findBySessionId(sessionId: String): ContainerImportJob?

    @Query("SELECT * FROM ContainerImportJob where cijUid = :uploadId")
    abstract fun findByUid(uploadId: Long): ContainerImportJob?

    @Query("""
            SELECT * 
              FROM ContainerImportJob 
             WHERE cijJobStatus = $QUEUED
                   AND (NOT cijImportCompleted OR 
                   (SELECT connectivityState 
                      FROM ConnectivityStatus)
                   IN ($STATE_METERED, $STATE_UNMETERED))
             LIMIT 10""")
    abstract fun findJobs(): LiveData<List<ContainerImportJob>>

    @Query("UPDATE ContainerImportJob SET cijJobStatus = $QUEUED WHERE cijUid = :uploadJobId AND cijJobStatus = $NOT_QUEUED")
    abstract suspend fun setStatusToQueueAsync(uploadJobId: Long)

    @Query("UPDATE ContainerImportJob SET cijBytesSoFar = :progress, cijContentLength = :contentLength WHERE cijUid = :uploadJobId")
    abstract fun updateProgress(progress: Long, contentLength: Long, uploadJobId: Long)

    @Query("UPDATE ContainerImportJob SET cijJobStatus = :status WHERE cijUid = :uploadJobId")
    abstract fun updateStatus(status: Int, uploadJobId: Long)

    @Query("""UPDATE ContainerImportJob 
                       SET cijImportCompleted = :importCompleted,
                           cijContainerUid = :containerUid
                     WHERE cijUid = :importJobUid""")
    abstract fun updateImportComplete(importCompleted: Boolean, containerUid: Long, importJobUid: Long)

    @Query("SELECT ContentEntry.title FROM ContainerImportJob " +
            "LEFT JOIN ContentEntry ON ContainerImportJob.cijContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContainerImportJob.cijUid = :importJobUid")
    abstract suspend fun getTitleOfEntry(importJobUid: Long): String?


    @Query("SELECT * From  ContainerImportJob WHERE ContainerImportJob.cijUid = :importJobUid")
    abstract fun getImportJobLiveData(importJobUid: Long): LiveData<ContainerImportJob?>

    @Query("UPDATE ContainerImportJob SET cijSessionId = :sessionId WHERE cijUid = :importJobUid")
    abstract suspend fun updateSessionId(importJobUid: Long, sessionId: String)

}