package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.JobStatus.NOT_QUEUED
import com.ustadmobile.core.db.JobStatus.QUEUED
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_METERED
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_UNMETERED
import com.ustadmobile.lib.db.entities.ContainerUploadJob
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin

@UmDao
@Dao
abstract class ContainerUploadJobDao : BaseDao<ContainerUploadJob> {

    @Query("SELECT * FROM ContainerUploadJob WHERE sessionId = :sessionId")
    abstract fun findBySessionId(sessionId: String): ContainerUploadJob?

    @Query("SELECT * FROM ContainerUploadJob where cujUid = :uploadId")
    abstract fun findByUid(uploadId: Long): ContainerUploadJob?

    @Update
    abstract override fun update(entity: ContainerUploadJob)

    @Query("""SELECT * FROM ContainerUploadJob WHERE jobStatus = $QUEUED
           AND (SELECT connectivityState from ConnectivityStatus WHERE connectivityState IN
            ($STATE_METERED, $STATE_UNMETERED))
             LIMIT 10""")
    abstract fun findJobs(): DoorLiveData<List<ContainerUploadJob>>

    @Query("UPDATE ContainerUploadJob SET jobStatus = $QUEUED WHERE cujUid = :uploadJobId AND jobStatus = $NOT_QUEUED")
    abstract suspend fun setStatusToQueueAsync(uploadJobId: Long)

    @Query("UPDATE ContainerUploadJob SET bytesSoFar = :progress, contentLength = :contentLength WHERE cujUid = :uploadJobId")
    abstract fun updateProgress(progress: Long, contentLength: Long, uploadJobId: Long)

    @Query("UPDATE ContainerUploadJob SET jobStatus = :status WHERE cujUid = :uploadJobId")
    abstract fun updateStatus(status: Int, uploadJobId: Long)

}