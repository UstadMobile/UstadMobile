package com.ustadmobile.core.db.dao

import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin.Companion.REL_TYPE_TRANSLATED_VERSION
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLangName
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import com.ustadmobile.lib.db.entities.UserSession
import kotlin.js.JsName

@DoorDao
@Repository
expect abstract class ContentEntryRelatedEntryJoinDao : BaseDao<ContentEntryRelatedEntryJoin> {

    @Query("""
     REPLACE INTO ContentEntryRelatedEntryJoinReplicate(cerejPk, cerejDestination)
      SELECT DISTINCT ContentEntryRelatedEntryJoin.cerejUid AS cerejPk,
             :newNodeId AS cerejDestination
        FROM ContentEntryRelatedEntryJoin
       WHERE ContentEntryRelatedEntryJoin.cerejLct != COALESCE(
             (SELECT cerejVersionId
                FROM ContentEntryRelatedEntryJoinReplicate
               WHERE cerejPk = ContentEntryRelatedEntryJoin.cerejUid
                 AND cerejDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(cerejPk, cerejDestination) DO UPDATE
             SET cerejPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([ContentEntryRelatedEntryJoin::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO ContentEntryRelatedEntryJoinReplicate(cerejPk, cerejDestination)
  SELECT DISTINCT ContentEntryRelatedEntryJoin.cerejUid AS cerejUid,
         UserSession.usClientNodeId AS cerejDestination
    FROM ChangeLog
         JOIN ContentEntryRelatedEntryJoin
             ON ChangeLog.chTableId = ${ContentEntryRelatedEntryJoin.TABLE_ID}
                AND ChangeLog.chEntityPk = ContentEntryRelatedEntryJoin.cerejUid
         JOIN UserSession ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND ContentEntryRelatedEntryJoin.cerejLct != COALESCE(
         (SELECT cerejVersionId
            FROM ContentEntryRelatedEntryJoinReplicate
           WHERE cerejPk = ContentEntryRelatedEntryJoin.cerejUid
             AND cerejDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(cerejPk, cerejDestination) DO UPDATE
     SET cerejPending = true
  */               
    """)
    @ReplicationRunOnChange([ContentEntryRelatedEntryJoin::class])
    @ReplicationCheckPendingNotificationsFor([ContentEntryRelatedEntryJoin::class])
    abstract suspend fun replicateOnChange()

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
    abstract fun findAllTranslationsWithContentEntryUid(contentEntryUid: Long): DataSourceFactory<Int, ContentEntryRelatedEntryJoinWithLanguage>

    @Update
    abstract override fun update(entity: ContentEntryRelatedEntryJoin)

}
