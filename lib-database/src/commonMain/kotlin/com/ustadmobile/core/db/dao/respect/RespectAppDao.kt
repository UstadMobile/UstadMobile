package com.ustadmobile.core.db.dao.respect

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.cash.paging.PagingSource
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.respect.RespectApp

@DoorDao
@Repository
expect abstract class RespectAppDao {

    @Insert
    abstract suspend fun insertAsync(respectApp: RespectApp)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertOrIgnore(respectApps: List<RespectApp>)

    @HttpAccessible
    @Query("""
        SELECT RespectApp.*
          FROM RespectApp
    """)
    abstract fun findAllAsPagingSource(): PagingSource<Int, RespectApp>

}