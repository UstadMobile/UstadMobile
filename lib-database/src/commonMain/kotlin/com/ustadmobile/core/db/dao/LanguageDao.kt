package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.lib.db.entities.Person
import kotlin.js.JsName

@Dao
@Repository
abstract class LanguageDao : BaseDao<Language> {

    @JsName("insertListAsync")
    @Insert
    abstract suspend fun insertListAsync(languageList: List<Language>)

    @Query("""
        SELECT Language.* 
        FROM Language
        WHERE name LIKE :searchText
        ORDER BY CASE(:sortOrder)
            WHEN $SORT_LANGNAME_ASC THEN Language.name
            ELSE ''
        END ASC,
        CASE(:sortOrder)
            WHEN $SORT_LANGNAME_DESC THEN Language.name
            ELSE ''
        END DESC
    """)
    abstract fun findLanguagesAsSource(sortOrder: Int, searchText: String): DataSource.Factory<Int, Language>

    @Query("""SELECT * FROM Language""")
    abstract fun findLanguagesList(): List<Language>

    @Query("SELECT * FROM Language WHERE name = :name LIMIT 1")
    abstract fun findByName(name: String): Language?

    @Query("SELECT * FROM Language WHERE iso_639_1_standard = :langCode LIMIT 1")
    abstract fun findByTwoCode(langCode: String): Language?

    @Query("SELECT * FROM Language WHERE iso_639_1_standard = :langCode LIMIT 1")
    abstract suspend fun findByTwoCodeAsync(langCode: String): Language?

    @Query("SELECT * FROM LANGUAGE WHERE iso_639_3_standard = :langCode OR iso_639_2_standard = :langCode LIMIT 1 ")
    abstract fun findByThreeCode(langCode: String): Language?

    @Query("SELECT COUNT(*) FROM LANGUAGE")
    abstract fun totalLanguageCount(): Int

    @Update
    abstract override fun update(entity: Language)

    @Query("SELECT *  FROM LANGUAGE where langUid = :primaryLanguageUid LIMIT 1")
    abstract fun findByUid(primaryLanguageUid: Long): Language?

    @Query("SELECT *  FROM LANGUAGE where langUid = :primaryLanguageUid LIMIT 1")
    abstract suspend fun findByUidAsync(primaryLanguageUid: Long): Language?

    @Update
    abstract suspend fun updateAsync(entity: Language): Int

    companion object  {

        const val SORT_LANGNAME_ASC = 1

        const val SORT_LANGNAME_DESC = 2

    }
}
