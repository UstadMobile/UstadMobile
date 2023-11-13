package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.LanguageVariant

@DoorDao
@Repository
expect abstract class LanguageVariantDao : BaseDao<LanguageVariant> {

    @Query("SELECT * FROM LanguageVariant WHERE countryCode = :countryCode LIMIT 1")
    abstract fun findByCode(countryCode: String): LanguageVariant?

}
