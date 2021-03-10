package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.Site

@Dao
@Repository
abstract class SiteDao: BaseDao<Site>{

    @Query("SELECT * FROM Site LIMIT 1")
    abstract fun getSite(): Site?

    @Query("SELECT * FROM Site LIMIT 1")
    abstract suspend fun getSiteAsync(): Site?

    @Update
    abstract suspend fun updateAsync(workspace: Site)

}