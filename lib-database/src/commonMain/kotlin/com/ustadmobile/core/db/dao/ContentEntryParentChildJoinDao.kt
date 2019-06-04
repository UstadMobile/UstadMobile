package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@Dao
@UmRepository
abstract class ContentEntryParentChildJoinDao : BaseDao<ContentEntryParentChildJoin> {

    @Query("SELECT ContentEntryParentChildJoin.* FROM " +
            "ContentEntryParentChildJoin " +
            "LEFT JOIN ContentEntry parentEntry ON ContentEntryParentChildJoin.cepcjParentContentEntryUid = parentEntry.contentEntryUid " +
            "LEFT JOIN ContentEntry childEntry ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = childEntry.contentEntryUid " +
            "WHERE parentEntry.publik AND childEntry.publik")
    abstract fun publicContentEntryParentChildJoins(): List<ContentEntryParentChildJoin>

    @Query("SELECT * FROM ContentEntryParentChildJoin")
    abstract fun all(): List<ContentEntryParentChildJoin>


    @Query("SELECT * FROM ContentEntryParentChildJoin WHERE " + "cepcjChildContentEntryUid = :childEntryContentUid LIMIT 1")
    abstract fun findParentByChildUuids(childEntryContentUid: Long): ContentEntryParentChildJoin?

    @Query("SELECT * FROM ContentEntryParentChildJoin WHERE " + "cepcjChildContentEntryUid = :childEntryContentUid")
    abstract fun findListOfParentsByChildUuid(childEntryContentUid: Long): List<ContentEntryParentChildJoin>

    @Query("SELECT * FROM ContentEntryParentChildJoin WHERE " + "cepcjParentContentEntryUid = :parentUid AND cepcjChildContentEntryUid = :childUid LIMIT 1")
    abstract fun findJoinByParentChildUuids(parentUid: Long, childUid: Long): ContentEntryParentChildJoin?

    @Update
    abstract override fun update(entity: ContentEntryParentChildJoin)
}
