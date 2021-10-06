package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem

@Dao
abstract class ContentJobDao {

    @Insert
    abstract suspend fun insertAsync(contentJob: ContentJob): Long

    @Query("""
        SELECT ContentJob.*
          FROM ContentJob
         WHERE cjUid = :cjUid 
    """)
    abstract fun findByUid(cjUid: Long): ContentJob?

    @Query("""
        SELECT ContentJob.*
          FROM ContentJob
         WHERE cjUid = :cjUid
    """)
    abstract fun findLiveDataByUid(cjUid: Long): DoorLiveData<ContentJob?>


}