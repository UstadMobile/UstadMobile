package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.db.entities.ContainerUploadJob
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin

@UmDao
@Dao
abstract class ContainerUploadJobDao : BaseDao<ContainerUploadJob> {

    @Query("SELECT * FROM ContainerUploadJob WHERE sessionId = :sessionId")
    abstract fun findBySessionId(sessionId: String): ContainerUploadJob?

    @Query("UPDATE ContainerUploadJob SET bytesSoFar = :endedAt WHERE sessionId = :sessionId")
    abstract fun updateBytesSoFarForSessionWithId(sessionId: String, endedAt: Long)

    @Query("UPDATE ContainerUploadJob SET jobStatus = :downloadStatus WHERE sessionId = :sessionId")
    abstract fun setJobStatus(downloadStatus: Int, sessionId: String)

    @Query("SELECT * FROM ContainerUploadJob where cujUid = :uploadId")
    abstract fun findByUid(uploadId: Long): ContainerUploadJob?

    @Update
    abstract override fun update(entity: ContainerUploadJob)

}