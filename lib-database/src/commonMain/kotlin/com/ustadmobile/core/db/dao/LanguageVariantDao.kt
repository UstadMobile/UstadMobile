package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.LanguageVariant
import com.ustadmobile.lib.db.entities.UserSession

@DoorDao
@Repository
expect abstract class LanguageVariantDao : BaseDao<LanguageVariant> {

    @Query("""
     REPLACE INTO LanguageVariantReplicate(lvPk, lvDestination)
      SELECT DISTINCT LanguageVariant.langVariantUid AS lvPk,
             :newNodeId AS lvDestination
        FROM LanguageVariant
       WHERE LanguageVariant.langVariantLct != COALESCE(
             (SELECT lvVersionId
                FROM LanguageVariantReplicate
               WHERE lvPk = LanguageVariant.langVariantUid
                 AND lvDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(lvPk, lvDestination) DO UPDATE
             SET lvPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([LanguageVariant::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO LanguageVariantReplicate(lvPk, lvDestination)
  SELECT DISTINCT LanguageVariant.langVariantUid AS lvUid,
         UserSession.usClientNodeId AS lvDestination
    FROM ChangeLog
         JOIN LanguageVariant
             ON ChangeLog.chTableId = ${LanguageVariant.TABLE_ID}
                AND ChangeLog.chEntityPk = LanguageVariant.langVariantUid
         JOIN UserSession ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND LanguageVariant.langVariantLct != COALESCE(
         (SELECT lvVersionId
            FROM LanguageVariantReplicate
           WHERE lvPk = LanguageVariant.langVariantUid
             AND lvDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(lvPk, lvDestination) DO UPDATE
     SET lvPending = true
  */               
    """)
    @ReplicationRunOnChange([LanguageVariant::class])
    @ReplicationCheckPendingNotificationsFor([LanguageVariant::class])
    abstract suspend fun replicateOnChange()

    @Query("SELECT * FROM LanguageVariant WHERE countryCode = :countryCode LIMIT 1")
    abstract fun findByCode(countryCode: String): LanguageVariant?

}
