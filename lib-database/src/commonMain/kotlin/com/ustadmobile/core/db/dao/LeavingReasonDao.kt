package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.LeavingReason

@Repository
@Dao
abstract class LeavingReasonDao : BaseDao<LeavingReason> {

    @Query("""SELECT * FROM LeavingReason""")
    abstract fun findAllReasons(): DataSource.Factory<Int, LeavingReason>


}