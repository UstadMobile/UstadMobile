package com.ustadmobile.libcache.db.dao

import androidx.room.Insert
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.libcache.db.entities.ResponseBody

@DoorDao
expect abstract class ResponseBodyDao {

    @Insert
    abstract fun insertList(responseBodies: List<ResponseBody>)

}