package com.ustadmobile.core.db.dao

import androidx.room.Insert
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.OfflineItem


@DoorDao
@Repository
expect abstract class OfflineItemDao {

    @Insert
    abstract suspend fun insertAsync(item: OfflineItem)

}