package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.lib.db.entities.ContainerETag

@DoorDao
expect abstract class ContainerETagDao : BaseDao<ContainerETag> {

    @Query("SELECT cetag FROM ContainerETag WHERE ceContainerUid = :containerUid")
    abstract fun getEtagOfContainer(containerUid: Long): String?

}