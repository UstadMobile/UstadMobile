package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ContentCategory

@Repository
@Dao
abstract class ContentCategoryDao : BaseDao<ContentCategory> {

    @Query("SELECT ContentCategory.* FROM ContentCategory")
    abstract fun publicContentCategories(): List<ContentCategory>

    @Query("SELECT * FROM ContentCategory WHERE " + "ctnCatContentCategorySchemaUid = :schemaId AND name = :name")
    abstract fun findCategoryBySchemaIdAndName(schemaId: Long, name: String): ContentCategory?
}
