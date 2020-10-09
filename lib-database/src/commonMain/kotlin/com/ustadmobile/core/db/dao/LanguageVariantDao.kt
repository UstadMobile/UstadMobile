package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.LanguageVariant

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@Dao
@UmRepository
abstract class LanguageVariantDao : BaseDao<LanguageVariant> {

    @Query("SELECT * FROM LanguageVariant WHERE countryCode = :countryCode LIMIT 1")
    abstract fun findByCode(countryCode: String): LanguageVariant?

}
