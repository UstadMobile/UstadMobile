package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ContentCategory

@Repository
@DoorDao
expect abstract class ContentCategoryDao : BaseDao<ContentCategory> {

    @Query("SELECT ContentCategory.* FROM ContentCategory")
    abstract fun publicContentCategories(): List<ContentCategory>

    @Query("SELECT * FROM ContentCategory WHERE " + "ctnCatContentCategorySchemaUid = :schemaId AND name = :name")
    abstract fun findCategoryBySchemaIdAndName(schemaId: Long, name: String): ContentCategory?
}
