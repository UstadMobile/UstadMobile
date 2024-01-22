package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ContentCategorySchema

@DoorDao
@Repository
expect abstract class ContentCategorySchemaDao : BaseDao<ContentCategorySchema> {


    @Query("SELECT ContentCategorySchema.* FROM ContentCategorySchema")
    abstract fun publicContentCategorySchemas(): List<ContentCategorySchema>

    @Query("SELECT * FROM ContentCategorySchema WHERE schemaUrl = :schemaUrl")
    abstract fun findBySchemaUrl(schemaUrl: String): ContentCategorySchema?

}
