package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.LanguageVariant

@Dao
@Repository
abstract class LanguageVariantDao : BaseDao<LanguageVariant> {

    @Query("SELECT * FROM LanguageVariant WHERE countryCode = :countryCode LIMIT 1")
    abstract fun findByCode(countryCode: String): LanguageVariant?

}
