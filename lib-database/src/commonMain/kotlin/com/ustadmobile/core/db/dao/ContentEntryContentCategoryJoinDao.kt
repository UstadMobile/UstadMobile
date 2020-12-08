package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ContentEntryContentCategoryJoin

@Dao
@Repository
abstract class ContentEntryContentCategoryJoinDao : BaseDao<ContentEntryContentCategoryJoin> {

    @Query("SELECT ContentEntryContentCategoryJoin.* FROM ContentEntryContentCategoryJoin " +
            "LEFT JOIN ContentEntry ON ContentEntryContentCategoryJoin.ceccjContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntry.publik")
    abstract fun publicContentEntryContentCategoryJoins(): List<ContentEntryContentCategoryJoin>

    @Query("SELECT * from ContentEntryContentCategoryJoin WHERE " + "ceccjContentCategoryUid = :categoryUid AND ceccjContentEntryUid = :contentEntry")
    abstract fun findJoinByParentChildUuids(categoryUid: Long, contentEntry: Long): ContentEntryContentCategoryJoin?

}
