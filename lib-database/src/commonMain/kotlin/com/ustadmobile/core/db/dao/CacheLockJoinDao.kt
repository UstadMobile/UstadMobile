package com.ustadmobile.core.db.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.lib.db.entities.CacheLockJoin
import com.ustadmobile.lib.db.entities.CacheLockJoin.Companion.STATUS_PENDING_DELETE
import com.ustadmobile.lib.db.entities.CacheLockJoin.Companion.STATUS_PENDING_CREATION

@DoorDao
expect abstract class CacheLockJoinDao {

    @Query("""
        SELECT CacheLockJoin.*
          FROM CacheLockJoin
         WHERE CacheLockJoin.cljStatus = $STATUS_PENDING_CREATION 
            OR CacheLockJoin.cljStatus = $STATUS_PENDING_DELETE
    """)
    abstract suspend fun findPendingLocks(): List<CacheLockJoin>

    @Query("""
        UPDATE CacheLockJoin
           SET cljLockId = :lockId,
               cljStatus = :status
         WHERE cljId = :uid   
    """)
    abstract suspend fun updateLockIdAndStatus(
        uid: Int,
        lockId: Long,
        status: Int
    )

    @Insert
    abstract suspend fun insertListAsync(list: List<CacheLockJoin>)

    @Query("""
        SELECT CacheLockJoin.*
          FROM CacheLockJoin
         WHERE CacheLockJoin.cljTableId = :tableId
           AND CacheLockJoin.cljEntityUid = :entityUid
    """)
    abstract suspend fun findByTableIdAndEntityUid(
        tableId: Int,
        entityUid: Long,
    ): List<CacheLockJoin>

    @Delete
    abstract suspend fun deleteListAsync(list: List<CacheLockJoin>)

}