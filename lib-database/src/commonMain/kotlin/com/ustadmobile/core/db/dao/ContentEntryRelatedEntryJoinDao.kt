package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin.Companion.REL_TYPE_TRANSLATED_VERSION
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import kotlin.js.JsName

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@Dao
@UmRepository
abstract class ContentEntryRelatedEntryJoinDao : BaseDao<ContentEntryRelatedEntryJoin> {

    @Query("SELECT ContentEntryRelatedEntryJoin.* FROM ContentEntryRelatedEntryJoin " +
            "LEFT JOIN ContentEntry ON ContentEntryRelatedEntryJoin.cerejRelatedEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntry.publik")
    @JsName("publicContentEntryRelatedEntryJoins")
    abstract fun publicContentEntryRelatedEntryJoins(): List<ContentEntryRelatedEntryJoin>

    @Query("SELECT * FROM ContentEntryRelatedEntryJoin WHERE " + "cerejRelatedEntryUid = :contentEntryUid LIMIT 1")
    @JsName("findPrimaryByTranslation")
    abstract fun findPrimaryByTranslation(contentEntryUid: Long): ContentEntryRelatedEntryJoin?


    @Query("SELECT ContentEntryRelatedEntryJoin.cerejContentEntryUid, ContentEntryRelatedEntryJoin.cerejRelatedEntryUid," +
            " CASE ContentEntryRelatedEntryJoin.cerejRelatedEntryUid" +
            " WHEN :contentEntryUid THEN (SELECT name FROM Language WHERE langUid = (SELECT primaryLanguageUid FROM ContentEntry WHERE contentEntryUid = ContentEntryRelatedEntryJoin.cerejContentEntryUid))" +
            " ELSE Language.name" +
            " END languageName" +
            " FROM ContentEntryRelatedEntryJoin" +
            " LEFT JOIN Language ON ContentEntryRelatedEntryJoin.cerejRelLanguageUid = Language.langUid" +
            " WHERE" +
            " (ContentEntryRelatedEntryJoin.cerejContentEntryUid = :contentEntryUid" +
            " OR ContentEntryRelatedEntryJoin.cerejContentEntryUid IN" +
            " (SELECT cerejContentEntryUid FROM ContentEntryRelatedEntryJoin WHERE cerejRelatedEntryUid = :contentEntryUid))" +
            " AND ContentEntryRelatedEntryJoin.relType = " + REL_TYPE_TRANSLATED_VERSION)
    @JsName("findAllTranslationsForContentEntryAsync")
    abstract suspend fun findAllTranslationsForContentEntryAsync(contentEntryUid: Long): List<ContentEntryRelatedEntryJoinWithLanguage>

    @Update
    abstract override fun update(entity: ContentEntryRelatedEntryJoin)

}
