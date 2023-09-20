package com.ustadmobile.core.db.dao

import app.cash.paging.PagingSource
import androidx.room.*
import com.ustadmobile.core.db.dao.LanguageDaoCommon.SORT_LANGNAME_ASC
import com.ustadmobile.core.db.dao.LanguageDaoCommon.SORT_LANGNAME_DESC
import com.ustadmobile.core.db.dao.LanguageDaoCommon.SORT_THREE_LETTER_ASC
import com.ustadmobile.core.db.dao.LanguageDaoCommon.SORT_THREE_LETTER_DESC
import com.ustadmobile.core.db.dao.LanguageDaoCommon.SORT_TWO_LETTER_ASC
import com.ustadmobile.core.db.dao.LanguageDaoCommon.SORT_TWO_LETTER_DESC
import kotlinx.coroutines.flow.Flow
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*

@DoorDao
@Repository
expect abstract class LanguageDao : BaseDao<Language> {

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
    abstract fun findLanguagesAsSource(sortOrder: Int, searchText: String): PagingSource<Int, Language>

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
    abstract fun findAllLanguageLive(): Flow<List<Language>>

    @Query("SELECT langUid FROM LANGUAGE WHERE langUid IN (:uidList)")
    abstract fun findByUidList(uidList: List<Long>): List<Long>


    @Query("""
        UPDATE Language 
           SET languageActive = :toggleVisibility, 
               langLct = :updateTime
         WHERE langUid IN (:selectedItem)""")
    abstract suspend fun toggleVisibilityLanguage(
        toggleVisibility: Boolean,
        selectedItem: List<Long>,
        updateTime: Long
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replaceList(entityList: List<Language>)


}
