package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.Person
import kotlin.js.JsName

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@Dao
@UmRepository
abstract class LanguageDao : BaseDao<Language> {

    @JsName("insertListAsync")
    @Insert
    abstract suspend fun insertListAsync(languageList: List<Language>)

    @Query("SELECT * FROM Language")
    abstract fun publicLanguages(): List<Language>


    @Query("SELECT * FROM Language WHERE name = :name LIMIT 1")
    abstract fun findByName(name: String): Language?

    @Query("SELECT * FROM Language WHERE iso_639_1_standard = :langCode LIMIT 1")
    abstract fun findByTwoCode(langCode: String): Language?

    @Query("SELECT COUNT(*) FROM LANGUAGE")
    abstract fun totalLanguageCount(): Int

    @Update
    abstract override fun update(entity: Language)
}
