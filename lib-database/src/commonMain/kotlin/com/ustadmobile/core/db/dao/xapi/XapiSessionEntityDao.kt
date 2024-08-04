package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity

@DoorDao
@Repository
expect abstract class XapiSessionEntityDao {

    @Insert
    abstract suspend fun insertAsync(xapiSessionEntity: XapiSessionEntity)

    @Query("""
        SELECT XapiSessionEntity.*
          FROM XapiSessionEntity
         WHERE XapiSessionEntity.xseUid = :uid
    """)
    abstract suspend fun findByUidAsync(uid: Long): XapiSessionEntity?

    @Query("""
        UPDATE XapiSessionEntity
           SET xseCompleted = :completed,
               xseLastMod = :time
         WHERE xseUid = :xseUid

    """)
    abstract suspend fun updateLatestAsComplete(
        completed: Boolean,
        time: Long,
        xseUid: Long,
    )

    @Query("""
        SELECT XapiSessionEntity.*
          FROM XapiSessionEntity
         WHERE XapiSessionEntity.xseRootActivityUid = :xseRootActivityUid
           AND XapiSessionEntity.xseActorUid = :xseActorUid
           AND (   CAST(:requireNotCompleted AS INTEGER) = 0 
                OR CAST(XapiSessionEntity.xseCompleted AS INTEGER) = 0)
    """)
    abstract suspend fun findPendingSessionByActorAndActivityUid(
        xseActorUid: Long,
        xseRootActivityUid: Long,
        requireNotCompleted: Boolean,
    ): XapiSessionEntity?

}