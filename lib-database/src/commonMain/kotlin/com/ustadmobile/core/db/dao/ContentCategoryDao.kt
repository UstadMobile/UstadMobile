package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ContentCategory

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@Dao
@UmRepository
abstract class ContentCategoryDao : BaseDao<ContentCategory> {

    @Query("SELECT ContentCategory.* FROM ContentCategory")
    abstract fun publicContentCategories(): List<ContentCategory>

    @Query("SELECT * FROM ContentCategory WHERE " + "ctnCatContentCategorySchemaUid = :schemaId AND name = :name")
    abstract fun findCategoryBySchemaIdAndName(schemaId: Long, name: String): ContentCategory?
}
