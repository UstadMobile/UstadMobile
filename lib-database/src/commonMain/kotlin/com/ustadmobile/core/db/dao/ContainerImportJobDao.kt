package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.core.db.JobStatus.NOT_QUEUED
import com.ustadmobile.core.db.JobStatus.QUEUED
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_METERED
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_UNMETERED
import com.ustadmobile.lib.db.entities.ContainerImportJob

@UmDao
@Dao
abstract class ContainerImportJobDao : BaseDao<ContainerImportJob> {

    @Query("SELECT * FROM ContainerImportJob WHERE sessionId = :sessionId")
    abstract fun findBySessionId(sessionId: String): ContainerImportJob?

    @Query("SELECT * FROM ContainerImportJob where cujUid = :uploadId")
    abstract fun findByUid(uploadId: Long): ContainerImportJob?

    @Query("""SELECT * FROM ContainerImportJob WHERE jobStatus = $QUEUED
           AND (NOT importCompleted OR (SELECT connectivityState from ConnectivityStatus WHERE connectivityState IN
            ($STATE_METERED, $STATE_UNMETERED)))
             LIMIT 10""")
    abstract fun findJobs(): DoorLiveData<List<ContainerImportJob>>

    @Query("UPDATE ContainerImportJob SET jobStatus = $QUEUED WHERE cujUid = :uploadJobId AND jobStatus = $NOT_QUEUED")
    abstract suspend fun setStatusToQueueAsync(uploadJobId: Long)

    @Query("UPDATE ContainerImportJob SET bytesSoFar = :progress, contentLength = :contentLength WHERE cujUid = :uploadJobId")
    abstract fun updateProgress(progress: Long, contentLength: Long, uploadJobId: Long)

    @Query("UPDATE ContainerImportJob SET jobStatus = :status WHERE cujUid = :uploadJobId")
    abstract fun updateStatus(status: Int, uploadJobId: Long)

    @Query("UPDATE ContainerImportJob SET importCompleted = :importCompleted")
    abstract fun updateImportComplete(importCompleted: Boolean = true)

    @Query("SELECT ContentEntry.title FROM ContainerImportJob " +
            "LEFT JOIN ContentEntry ON ContainerImportJob.contentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContainerImportJob.cujUid = :importJobUid")
    abstract suspend fun getTitleOfEntry(importJobUid: Long): String?


    @Query("SELECT * From  ContainerImportJob WHERE ContainerImportJob.cujUid = :importJobUid")
    abstract fun getImportJobLiveData(importJobUid: Long): DoorLiveData<ContainerImportJob?>

}