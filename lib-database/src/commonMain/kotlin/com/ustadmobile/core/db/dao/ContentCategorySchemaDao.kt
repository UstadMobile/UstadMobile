package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ContentCategorySchema
import com.ustadmobile.lib.db.entities.UserSession

@DoorDao
@Repository
expect abstract class ContentCategorySchemaDao : BaseDao<ContentCategorySchema> {

    @Query("""
     REPLACE INTO ContentCategorySchemaReplicate(ccsPk, ccsDestination)
      SELECT DISTINCT ContentCategorySchema.contentCategorySchemaUid AS ccsPk,
             :newNodeId AS ccsDestination
        FROM ContentCategorySchema
       WHERE ContentCategorySchema.contentCategorySchemaLct != COALESCE(
             (SELECT ccsVersionId
                FROM ContentCategorySchemaReplicate
               WHERE ccsPk = ContentCategorySchema.contentCategorySchemaUid
                 AND ccsDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(ccsPk, ccsDestination) DO UPDATE
             SET ccsPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([ContentCategorySchema::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO ContentCategorySchemaReplicate(ccsPk, ccsDestination)
  SELECT DISTINCT ContentCategorySchema.contentCategorySchemaUid AS ccsUid,
         UserSession.usClientNodeId AS ccsDestination
    FROM ChangeLog
         JOIN ContentCategorySchema
             ON ChangeLog.chTableId = 2
                AND ChangeLog.chEntityPk = ContentCategorySchema.contentCategorySchemaUid
         JOIN UserSession ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND ContentCategorySchema.contentCategorySchemaLct != COALESCE(
         (SELECT ccsVersionId
            FROM ContentCategorySchemaReplicate
           WHERE ccsPk = ContentCategorySchema.contentCategorySchemaUid
             AND ccsDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(ccsPk, ccsDestination) DO UPDATE
     SET ccsPending = true
  */               
    """)
    @ReplicationRunOnChange([ContentCategorySchema::class])
    @ReplicationCheckPendingNotificationsFor([ContentCategorySchema::class])
    abstract suspend fun replicateOnChange()

    @Query("SELECT ContentCategorySchema.* FROM ContentCategorySchema")
    abstract fun publicContentCategorySchemas(): List<ContentCategorySchema>

    @Query("SELECT * FROM ContentCategorySchema WHERE schemaUrl = :schemaUrl")
    abstract fun findBySchemaUrl(schemaUrl: String): ContentCategorySchema?

}
