package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.db.entities.ContainerUploadJob

@UmDao
@Dao
abstract class ContainerUploadJobDao : BaseDao<ContainerUploadJob> {

    @Query("SELECT * FROM ContainerUploadJob WHERE sessionId = :sessionId")
    abstract fun findBySessionId(sessionId: String): ContainerUploadJob?

    @Query("UPDATE ContainerUploadJob SET bytesSoFar = :endedAt WHERE sessionId = :sessionId")
    abstract fun updateJob(sessionId: String, endedAt: Long)

    @Query("UPDATE ContainerUploadJob SET jobStatus = :downloadStatus WHERE sessionId = :sessionId")
    abstract fun setJobStatus(downloadStatus: Int, sessionId: String)

}