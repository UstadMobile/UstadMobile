package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ContentCategorySchema

@Dao
@Repository
abstract class ContentCategorySchemaDao : BaseDao<ContentCategorySchema> {

    @Query("SELECT ContentCategorySchema.* FROM ContentCategorySchema")
    abstract fun publicContentCategorySchemas(): List<ContentCategorySchema>

    @Query("SELECT * FROM ContentCategorySchema WHERE schemaUrl = :schemaUrl")
    abstract fun findBySchemaUrl(schemaUrl: String): ContentCategorySchema?

}
