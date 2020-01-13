package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ContainerETag

@Dao
@UmRepository
abstract class ContainerETagDao : BaseDao<ContainerETag> {

    @Query("SELECT cetag FROME ContainerEtag WHERE ceContainerUid = :containerUid")
    abstract fun getEtagOfContainer(containerUid: Long): String?

}