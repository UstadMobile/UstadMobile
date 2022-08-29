package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.UserSession
import com.ustadmobile.lib.db.entities.XLangMapEntry
import kotlinx.serialization.Serializable
import kotlin.js.JsName

@DoorDao
@Repository
expect abstract class XLangMapEntryDao : BaseDao<XLangMapEntry> {

    @Query("""
         REPLACE INTO XLangMapEntryReplicate(xlmePk, xlmeDestination)
          SELECT DISTINCT XLangMapEntry.statementLangMapUid AS xlmePk,
                 :newNodeId AS xlmeDestination
            FROM XLangMapEntry
                 JOIN UserSession ON UserSession.usClientNodeId = :newNodeId
             --notpsql      
             WHERE XLangMapEntry.statementLangMapLct != COALESCE(
                 (SELECT xlmeVersionId
                    FROM XLangMapEntryReplicate
                   WHERE xlmePk = XLangMapEntry.statementLangMapUid
                     AND xlmeDestination = UserSession.usClientNodeId), 0)
             --endnotpsql         
          /*psql ON CONFLICT(xlmePk, xlmeDestination) DO UPDATE
                 SET xlmePending = (SELECT XLangMapEntry.statementLangMapLct
                                      FROM XLangmapEntry
                                     WHERE XLangmapEntry.statementLangMapUid = EXCLUDED.xlmePk)
                                        != XLangMapEntryReplicate.xlmeVersionId
          */       
     """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([XLangMapEntry::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO XLangMapEntryReplicate(xlmePk, xlmeDestination)
  SELECT DISTINCT XLangMapEntry.statementLangMapUid AS xlmeUid,
         UserSession.usClientNodeId AS xlmeDestination
    FROM ChangeLog
         JOIN XLangMapEntry
             ON ChangeLog.chTableId = ${XLangMapEntry.TABLE_ID}
                AND ChangeLog.chEntityPk = XLangMapEntry.statementLangMapUid
         JOIN UserSession ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND XLangMapEntry.statementLangMapLct != COALESCE(
         (SELECT xlmeVersionId
            FROM XLangMapEntryReplicate
           WHERE xlmePk = XLangMapEntry.statementLangMapUid
             AND xlmeDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(xlmePk, xlmeDestination) DO UPDATE
     SET xlmePending = true
  */               
    """)
    @ReplicationRunOnChange([XLangMapEntry::class])
    @ReplicationCheckPendingNotificationsFor([XLangMapEntry::class])
    abstract suspend fun replicateOnChange()

    @JsName("getValuesWithListOfId")
    @Query("SELECT * FROM XLangMapEntry WHERE objectLangMapUid IN (:ids)")
    abstract suspend fun getValuesWithListOfId(ids: List<Int>): List<XLangMapEntry>


    @Query("""SELECT * FROM XLangMapEntry WHERE 
            verbLangMapUid = :verbUid AND languageLangMapUid = :langMapUid LIMIT 1""")
    abstract fun getXLangMapFromVerb(verbUid: Long, langMapUid: Long): XLangMapEntry?

    @Query("""SELECT * FROM XLangMapEntry WHERE 
            objectLangMapUid = :objectUid AND languageLangMapUid = :langMapUid LIMIT 1""")
    abstract fun getXLangMapFromObject(objectUid: Long, langMapUid: Long): XLangMapEntry?


}
