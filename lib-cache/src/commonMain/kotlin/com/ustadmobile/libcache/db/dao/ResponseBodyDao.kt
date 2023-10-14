package com.ustadmobile.libcache.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.libcache.db.entities.RequestedEntry
import com.ustadmobile.libcache.db.entities.ResponseBody

@DoorDao
expect abstract class ResponseBodyDao {

    @Insert
    abstract suspend fun insertListAsync(responseBodies: List<ResponseBody>)



}