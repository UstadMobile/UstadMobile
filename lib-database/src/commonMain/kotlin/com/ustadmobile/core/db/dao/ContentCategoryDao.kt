package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ContentCategory
import com.ustadmobile.lib.db.entities.UserSession

@Repository
@DoorDao
expect abstract class ContentCategoryDao : BaseDao<ContentCategory> {

    @Query("""
     REPLACE INTO ContentCategoryReplicate(ccPk, ccDestination)
      SELECT DISTINCT ContentCategory.contentCategoryUid AS ccPk,
             :newNodeId AS ccDestination
        FROM ContentCategory
       WHERE ContentCategory.contentCategoryLct != COALESCE(
             (SELECT ccVersionId
                FROM ContentCategoryReplicate
               WHERE ccPk = ContentCategory.contentCategoryUid
                 AND ccDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(ccPk, ccDestination) DO UPDATE
             SET ccPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([ContentCategory::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO ContentCategoryReplicate(ccPk, ccDestination)
  SELECT DISTINCT ContentCategory.contentCategoryUid AS ccUid,
         UserSession.usClientNodeId AS ccDestination
    FROM ChangeLog
         JOIN ContentCategory
             ON ChangeLog.chTableId = ${ContentCategory.TABLE_ID}
                AND ChangeLog.chEntityPk = ContentCategory.contentCategoryUid
         JOIN UserSession 
              ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND ContentCategory.contentCategoryLct != COALESCE(
         (SELECT ccVersionId
            FROM ContentCategoryReplicate
           WHERE ccPk = ContentCategory.contentCategoryUid
             AND ccDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(ccPk, ccDestination) DO UPDATE
     SET ccPending = true
  */               
    """)
    @ReplicationRunOnChange([ContentCategory::class])
    @ReplicationCheckPendingNotificationsFor([ContentCategory::class])
    abstract suspend fun replicateOnChange()


    @Query("SELECT ContentCategory.* FROM ContentCategory")
    abstract fun publicContentCategories(): List<ContentCategory>

    @Query("SELECT * FROM ContentCategory WHERE " + "ctnCatContentCategorySchemaUid = :schemaId AND name = :name")
    abstract fun findCategoryBySchemaIdAndName(schemaId: Long, name: String): ContentCategory?
}
