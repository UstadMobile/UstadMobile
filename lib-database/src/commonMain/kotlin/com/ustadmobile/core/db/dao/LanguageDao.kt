package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.Language
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
            WHEN $SORT_TWO_LETTER_ASC THEN Language.iso_639_1_standard 
            WHEN $SORT_THREE_LETTER_ASC THEN Language.iso_639_2_standard 
            ELSE ''
        END ASC,
        CASE(:sortOrder)
            WHEN $SORT_LANGNAME_DESC THEN Language.name 
            WHEN $SORT_TWO_LETTER_DESC THEN Language.iso_639_1_standard 
            WHEN $SORT_THREE_LETTER_DESC THEN Language.iso_639_2_standard 
            ELSE ''
        END DESC
    """)
    abstract fun findLanguagesAsSource(sortOrder: Int, searchText: String): DoorDataSourceFactory<Int, Language>

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

    @Query("SELECT * FROM LANGUAGE")
    abstract fun findAllLanguageLive(): DoorLiveData<List<Language>>

    @JsName("findByUidList")
    @Query("SELECT langUid FROM LANGUAGE WHERE langUid IN (:uidList)")
    abstract suspend fun findByUidList(uidList: List<Long>): List<Long>


    @Query("""UPDATE Language SET languageActive = :toggleVisibility, 
                langLastChangedBy =  COALESCE((SELECT nodeClientId FROM SyncNode LIMIT 1), 0) 
                WHERE langUid IN (:selectedItem)""")
    abstract suspend fun toggleVisibilityLanguage(toggleVisibility: Boolean, selectedItem: List<Long>)

    @JsName("replaceList")
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceList(entityList: List<Language>)

    suspend fun initPreloadedLanguages() {
        val uidsInserted = findByUidList(Language.FIXED_LANGUAGES.map { it.langUid })
        val templateListToInsert = Language.FIXED_LANGUAGES.filter { it.langUid !in uidsInserted }
        replaceList(templateListToInsert)
    }

    companion object  {

        const val SORT_LANGNAME_ASC = 1

        const val SORT_LANGNAME_DESC = 2

        const val SORT_TWO_LETTER_ASC = 3

        const val SORT_TWO_LETTER_DESC = 4

        const val SORT_THREE_LETTER_ASC = 5

        const val SORT_THREE_LETTER_DESC = 6

    }
}
