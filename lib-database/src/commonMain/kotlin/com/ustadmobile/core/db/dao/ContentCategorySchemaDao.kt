package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ContentCategorySchema

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@Dao
@UmRepository
abstract class ContentCategorySchemaDao : BaseDao<ContentCategorySchema> {

    @Query("SELECT ContentCategorySchema.* FROM ContentCategorySchema")
    abstract fun publicContentCategorySchemas(): List<ContentCategorySchema>

    @Query("SELECT * FROM ContentCategorySchema WHERE schemaUrl = :schemaUrl")
    abstract fun findBySchemaUrl(schemaUrl: String): ContentCategorySchema?

}
