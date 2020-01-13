package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.db.entities.ContainerETag

@Dao
abstract class ContainerETagDao : BaseDao<ContainerETag> {

    @Query("SELECT cetag FROM ContainerETag WHERE ceContainerUid = :containerUid")
    abstract fun getEtagOfContainer(containerUid: Long): String?

}