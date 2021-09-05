package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import com.ustadmobile.lib.db.entities.ContentJob

@Dao
abstract class ContentJobDao {

    @Insert
    abstract suspend fun insertAsync(contentJob: ContentJob)
}