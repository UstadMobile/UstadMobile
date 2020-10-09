package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
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

    @Query("SELECT * FROM Language ORDER BY name ASC")
    abstract fun publicLanguagesOrderByNameAsc(): DataSource.Factory<Int, Language>

    @Query("SELECT * FROM Language ORDER BY name DESC")
    abstract fun publicLanguagesOrderByNameDesc(): DataSource.Factory<Int, Language>

    @Query("SELECT * FROM Language WHERE name = :name LIMIT 1")
    abstract fun findByName(name: String): Language?

    @Query("SELECT * FROM Language WHERE iso_639_1_standard = :langCode LIMIT 1")
    abstract fun findByTwoCode(langCode: String): Language?

    @Query("SELECT * FROM LANGUAGE WHERE iso_639_3_standard = :langCode OR iso_639_2_standard = :langCode LIMIT 1 ")
    abstract fun findByThreeCode(langCode: String): Language?

    @Query("SELECT COUNT(*) FROM LANGUAGE")
    abstract fun totalLanguageCount(): Int

    @Update
    abstract override fun update(entity: Language)

    @Query("SELECT *  FROM LANGUAGE where langUid = :primaryLanguageUid LIMIT 1")
    abstract fun findByUid(primaryLanguageUid: Long): Language?
}
