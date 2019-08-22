package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.XObjectEntity

@Dao
@UmRepository
abstract class XObjectDao : BaseDao<XObjectEntity> {

    @Query("SELECT * from XObjectEntity WHERE objectId = :id")
    abstract fun findByObjectId(id: String?): XObjectEntity?

    @Query("SELECT xObjectUid FROM XObjectEntity WHERE objectContentEntryUid IN (:contentEntryUid)")
    abstract suspend fun findListOfObjectUidFromContentEntryUid(contentEntryUid: List<Long>): List<Long>
}
