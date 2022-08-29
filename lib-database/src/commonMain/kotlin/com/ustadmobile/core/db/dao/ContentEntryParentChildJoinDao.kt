package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.UserSession
import kotlin.js.JsName

data class UmContentEntriesWithFileSize(var numEntries: Int = 0, var fileSize: Long = 0L)

@Repository
@DoorDao
expect abstract class ContentEntryParentChildJoinDao : BaseDao<ContentEntryParentChildJoin> {

    @Query("""
     REPLACE INTO ContentEntryParentChildJoinReplicate(cepcjPk, cepcjDestination)
      SELECT DISTINCT ContentEntryParentChildJoin.cepcjUid AS cepcjUid,
             :newNodeId AS cepcjDestination
        FROM ContentEntryParentChildJoin
       WHERE ContentEntryParentChildJoin.cepcjLct != COALESCE(
             (SELECT cepcjVersionId
                FROM ContentEntryParentChildJoinReplicate
               WHERE cepcjPk = ContentEntryParentChildJoin.cepcjUid
                 AND cepcjDestination = :newNodeId), -1) 
      /*psql ON CONFLICT(cepcjPk, cepcjDestination) DO UPDATE
             SET cepcjPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([ContentEntryParentChildJoin::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
    REPLACE INTO ContentEntryParentChildJoinReplicate(cepcjPk, cepcjDestination)
    SELECT DISTINCT ContentEntryParentChildJoin.cepcjUid AS cepcjUid,
         UserSession.usClientNodeId AS cepcjDestination
    FROM ChangeLog
         JOIN ContentEntryParentChildJoin
             ON ChangeLog.chTableId = 7
                AND ChangeLog.chEntityPk = ContentEntryParentChildJoin.cepcjUid
         JOIN UserSession ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
    WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND ContentEntryParentChildJoin.cepcjLct != COALESCE(
         (SELECT cepcjVersionId
            FROM ContentEntryParentChildJoinReplicate
           WHERE cepcjPk = ContentEntryParentChildJoin.cepcjUid
             AND cepcjDestination = UserSession.usClientNodeId), 0)
    /*psql ON CONFLICT(cepcjPk, cepcjDestination) DO UPDATE
     SET cepcjPending = true
    */               
    """)
    @ReplicationRunOnChange([ContentEntryParentChildJoin::class])
    @ReplicationCheckPendingNotificationsFor([ContentEntryParentChildJoin::class])
    abstract suspend fun replicateOnChange()


    @JsName("insertListAsync")
    @Insert
    abstract suspend fun insertListAsync(entityList: List<ContentEntryParentChildJoin>)

    @Query("SELECT ContentEntryParentChildJoin.* FROM " +
            "ContentEntryParentChildJoin " +
            "LEFT JOIN ContentEntry parentEntry ON ContentEntryParentChildJoin.cepcjParentContentEntryUid = parentEntry.contentEntryUid " +
            "LEFT JOIN ContentEntry childEntry ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = childEntry.contentEntryUid " +
            "WHERE parentEntry.publik AND childEntry.publik")
    abstract fun publicContentEntryParentChildJoins(): List<ContentEntryParentChildJoin>

    @Query("SELECT * FROM ContentEntryParentChildJoin")
    abstract fun all(): List<ContentEntryParentChildJoin>


    @Query("SELECT * FROM ContentEntryParentChildJoin WHERE " + "cepcjChildContentEntryUid = :childEntryContentUid LIMIT 1")
    abstract fun findParentByChildUuids(childEntryContentUid: Long): ContentEntryParentChildJoin?

    @Query("SELECT * FROM ContentEntryParentChildJoin WHERE " + "cepcjChildContentEntryUid = :childEntryContentUid")
    abstract fun findListOfParentsByChildUuid(childEntryContentUid: Long): List<ContentEntryParentChildJoin>

    @Query("SELECT * FROM ContentEntryParentChildJoin WHERE " + "cepcjParentContentEntryUid = :parentUid")
    abstract fun findListOfChildsByParentUuid(parentUid: Long): List<ContentEntryParentChildJoin>

    @Query("SELECT * FROM ContentEntryParentChildJoin WHERE " + "cepcjParentContentEntryUid = :parentUid AND cepcjChildContentEntryUid = :childUid LIMIT 1")
    abstract fun findJoinByParentChildUuids(parentUid: Long, childUid: Long): ContentEntryParentChildJoin?

    @Query("SELECT ContentEntry.* FROM ContentEntry " +
            "WHERE NOT EXISTS(SELECT cepcjUid FROM ContentEntryParentChildJoin WHERE cepcjChildContentEntryUid = ContentEntry.contentEntryUid) " +
            "AND EXISTS(SELECT cepcjUid FROM ContentEntryParentChildJoin WHERE cepcjParentContentEntryUid = ContentEntry.contentEntryUid)")
    abstract suspend fun selectTopEntries(): List<ContentEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replaceList(entries: List<ContentEntryParentChildJoin>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertWithReplace(parentChildJoinDao: ContentEntryParentChildJoin)

    @Query("""
        UPDATE ContentEntryParentChildJoin 
           SET cepcjParentContentEntryUid = :contentEntryUid, 
               cepcjLct = :updateTime 
               WHERE cepcjUid IN (:selectedItems)
    """)
    abstract suspend fun moveListOfEntriesToNewParent(
        contentEntryUid: Long,
        selectedItems: List<Long>,
        updateTime: Long,
    )
}
