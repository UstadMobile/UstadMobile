package com.ustadmobile.core.db.dao

import com.ustadmobile.door.DoorDataSourceFactory
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin.Companion.REL_TYPE_TRANSLATED_VERSION
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLangName
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import kotlin.js.JsName

@Dao
@Repository
abstract class ContentEntryRelatedEntryJoinDao : BaseDao<ContentEntryRelatedEntryJoin> {

    @Query("SELECT ContentEntryRelatedEntryJoin.* FROM ContentEntryRelatedEntryJoin " +
            "LEFT JOIN ContentEntry ON ContentEntryRelatedEntryJoin.cerejRelatedEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntry.publik")
    @JsName("publicContentEntryRelatedEntryJoins")
    abstract fun publicContentEntryRelatedEntryJoins(): List<ContentEntryRelatedEntryJoin>

    @Query("SELECT * FROM ContentEntryRelatedEntryJoin WHERE " + "cerejRelatedEntryUid = :contentEntryUid LIMIT 1")
    @JsName("findPrimaryByTranslation")
    abstract fun findPrimaryByTranslation(contentEntryUid: Long): ContentEntryRelatedEntryJoin?


    @Deprecated("use findAllTranslationsWithContentEntryUid")
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
    abstract suspend fun findAllTranslationsForContentEntryAsync(contentEntryUid: Long): List<ContentEntryRelatedEntryJoinWithLangName>


    @Query("""SELECT ContentEntryRelatedEntryJoin.*, Language.* FROM ContentEntryRelatedEntryJoin
        LEFT JOIN Language ON ContentEntryRelatedEntryJoin.cerejRelLanguageUid = Language.langUid
        WHERE (ContentEntryRelatedEntryJoin.cerejContentEntryUid = :contentEntryUid
        OR ContentEntryRelatedEntryJoin.cerejContentEntryUid IN
        (SELECT cerejContentEntryUid FROM ContentEntryRelatedEntryJoin WHERE cerejRelatedEntryUid = :contentEntryUid))
        AND ContentEntryRelatedEntryJoin.relType = $REL_TYPE_TRANSLATED_VERSION
        ORDER BY Language.name""")
    @JsName("findAllTranslationsWithContentEntryUid")
    abstract fun findAllTranslationsWithContentEntryUid(contentEntryUid: Long): DoorDataSourceFactory<Int, ContentEntryRelatedEntryJoinWithLanguage>

    @Update
    abstract override fun update(entity: ContentEntryRelatedEntryJoin)

}
