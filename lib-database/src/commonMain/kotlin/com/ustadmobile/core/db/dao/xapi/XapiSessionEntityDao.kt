package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity

@DoorDao
@Repository
expect abstract class XapiSessionEntityDao {

    @Insert
    abstract suspend fun insertAsync(xapiSessionEntity: XapiSessionEntity)

}