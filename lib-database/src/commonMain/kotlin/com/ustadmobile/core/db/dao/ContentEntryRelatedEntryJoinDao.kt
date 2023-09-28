package com.ustadmobile.core.db.dao

import app.cash.paging.PagingSource
import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin.Companion.REL_TYPE_TRANSLATED_VERSION
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLangName
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage

@DoorDao
@Repository
expect abstract class ContentEntryRelatedEntryJoinDao : BaseDao<ContentEntryRelatedEntryJoin> {

    @Query("SELECT ContentEntryRelatedEntryJoin.* FROM ContentEntryRelatedEntryJoin " +
            "LEFT JOIN ContentEntry ON ContentEntryRelatedEntryJoin.cerejRelatedEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntry.publik")
    abstract fun publicContentEntryRelatedEntryJoins(): List<ContentEntryRelatedEntryJoin>

    @Query("SELECT * FROM ContentEntryRelatedEntryJoin WHERE " + "cerejRelatedEntryUid = :contentEntryUid LIMIT 1")
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
    abstract suspend fun findAllTranslationsForContentEntryAsync(contentEntryUid: Long): List<ContentEntryRelatedEntryJoinWithLangName>


    @Query("""SELECT ContentEntryRelatedEntryJoin.*, Language.* FROM ContentEntryRelatedEntryJoin
        LEFT JOIN Language ON ContentEntryRelatedEntryJoin.cerejRelLanguageUid = Language.langUid
        WHERE (ContentEntryRelatedEntryJoin.cerejContentEntryUid = :contentEntryUid
        OR ContentEntryRelatedEntryJoin.cerejContentEntryUid IN
        (SELECT cerejContentEntryUid FROM ContentEntryRelatedEntryJoin WHERE cerejRelatedEntryUid = :contentEntryUid))
        AND ContentEntryRelatedEntryJoin.relType = $REL_TYPE_TRANSLATED_VERSION
        ORDER BY Language.name""")
    abstract fun findAllTranslationsWithContentEntryUid(contentEntryUid: Long): PagingSource<Int, ContentEntryRelatedEntryJoinWithLanguage>

    @Update
    abstract override fun update(entity: ContentEntryRelatedEntryJoin)

}
