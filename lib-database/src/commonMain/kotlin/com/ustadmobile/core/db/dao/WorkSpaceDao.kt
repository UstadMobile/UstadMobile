package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.db.entities.WorkSpace

@Dao
abstract class WorkSpaceDao: BaseDao<WorkSpace>{

    @Query("SELECT * FROM WorkSpace LIMIT 1")
    abstract fun getWorkSpace(): WorkSpace?
}