package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.WorkSpace

@Dao
@Repository
abstract class WorkSpaceDao: BaseDao<WorkSpace>{

    @Query("SELECT * FROM WorkSpace LIMIT 1")
    abstract fun getWorkSpace(): WorkSpace?

    @Query("SELECT * FROM WorkSpace LIMIT 1")
    abstract suspend fun getWorkspaceAsync(): WorkSpace?

}