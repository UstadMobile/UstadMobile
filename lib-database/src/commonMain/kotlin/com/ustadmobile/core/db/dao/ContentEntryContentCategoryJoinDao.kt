package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ContentEntryContentCategoryJoin

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@Dao
@UmRepository
abstract class ContentEntryContentCategoryJoinDao : BaseDao<ContentEntryContentCategoryJoin> {

    @Query("SELECT ContentEntryContentCategoryJoin.* FROM ContentEntryContentCategoryJoin " +
            "LEFT JOIN ContentEntry ON ContentEntryContentCategoryJoin.ceccjContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntry.publik")
    abstract fun publicContentEntryContentCategoryJoins(): List<ContentEntryContentCategoryJoin>

    @Query("SELECT * from ContentEntryContentCategoryJoin WHERE " + "ceccjContentCategoryUid = :categoryUid AND ceccjContentEntryUid = :contentEntry")
    abstract fun findJoinByParentChildUuids(categoryUid: Long, contentEntry: Long): ContentEntryContentCategoryJoin?

}
