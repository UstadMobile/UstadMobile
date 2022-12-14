package com.ustadmobile.core.db.dao

import com.ustadmobile.door.paging.DataSourceFactory
import androidx.room.*
import com.ustadmobile.core.db.dao.LanguageDaoCommon.SORT_LANGNAME_ASC
import com.ustadmobile.core.db.dao.LanguageDaoCommon.SORT_LANGNAME_DESC
import com.ustadmobile.core.db.dao.LanguageDaoCommon.SORT_THREE_LETTER_ASC
import com.ustadmobile.core.db.dao.LanguageDaoCommon.SORT_THREE_LETTER_DESC
import com.ustadmobile.core.db.dao.LanguageDaoCommon.SORT_TWO_LETTER_ASC
import com.ustadmobile.core.db.dao.LanguageDaoCommon.SORT_TWO_LETTER_DESC
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*
import kotlin.js.JsName

@DoorDao
@Repository
expect abstract class LanguageDao : BaseDao<Language> {

    @Query("""
     REPLACE INTO LanguageReplicate(languagePk, languageDestination)
      SELECT DISTINCT Language.langUid AS languagePk,
             :newNodeId AS languageDestination
        FROM Language
       WHERE Language.langLct != COALESCE(
             (SELECT languageVersionId
                FROM LanguageReplicate
               WHERE languagePk = Language.langUid
                 AND languageDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(languagePk, languageDestination) DO UPDATE
             SET languagePending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([Language::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO LanguageReplicate(languagePk, languageDestination)
  SELECT DISTINCT Language.langUid AS languageUid,
         UserSession.usClientNodeId AS languageDestination
    FROM ChangeLog
         JOIN Language
             ON ChangeLog.chTableId = ${Language.TABLE_ID}
                AND ChangeLog.chEntityPk = Language.langUid
         JOIN UserSession ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND Language.langLct != COALESCE(
         (SELECT languageVersionId
            FROM LanguageReplicate
           WHERE languagePk = Language.langUid
             AND languageDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(languagePk, languageDestination) DO UPDATE
     SET languagePending = true
  */               
    """)
    @ReplicationRunOnChange([Language::class])
    @ReplicationCheckPendingNotificationsFor([Language::class])
    abstract suspend fun replicateOnChange()

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
    abstract fun findLanguagesAsSource(sortOrder: Int, searchText: String): DataSourceFactory<Int, Language>

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
    abstract fun findAllLanguageLive(): LiveData<List<Language>>

    @JsName("findByUidList")
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

    @JsName("replaceList")
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replaceList(entityList: List<Language>)


}
