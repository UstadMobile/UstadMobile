package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.SyncResult
import com.ustadmobile.lib.database.annotation.UmRepository

@UmRepository
@Dao
abstract class SyncResultDao : BaseDao<SyncResult> {

    @Query("SELECT timestamp from SyncResult ORDER BY timestamp DESC LIMIT 1")
    abstract suspend fun getLatestTimeStamp(): Long



    companion object {

    }
}
