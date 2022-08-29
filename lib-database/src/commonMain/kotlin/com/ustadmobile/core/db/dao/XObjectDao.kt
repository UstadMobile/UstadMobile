package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.UserSession
import com.ustadmobile.lib.db.entities.XObjectEntity
import kotlin.js.JsName

@DoorDao
@Repository
expect abstract class XObjectDao : BaseDao<XObjectEntity> {

    @Query("""
     REPLACE INTO XObjectEntityReplicate(xoePk, xoeDestination)
      SELECT DISTINCT XObjectEntity.xObjectUid AS xoePk,
             :newNodeId AS xoeDestination
        FROM XObjectEntity
       WHERE XObjectEntity.xObjectLct != COALESCE(
             (SELECT xoeVersionId
                FROM XObjectEntityReplicate
               WHERE xoePk = XObjectEntity.xObjectUid
                 AND xoeDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(xoePk, xoeDestination) DO UPDATE
             SET xoePending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([XObjectEntity::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO XObjectEntityReplicate(xoePk, xoeDestination)
  SELECT DISTINCT XObjectEntity.xObjectUid AS xoeUid,
         UserSession.usClientNodeId AS xoeDestination
    FROM ChangeLog
         JOIN XObjectEntity
             ON ChangeLog.chTableId = ${XObjectEntity.TABLE_ID}
                AND ChangeLog.chEntityPk = XObjectEntity.xObjectUid
         JOIN UserSession ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND XObjectEntity.xObjectLct != COALESCE(
         (SELECT xoeVersionId
            FROM XObjectEntityReplicate
           WHERE xoePk = XObjectEntity.xObjectUid
             AND xoeDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(xoePk, xoeDestination) DO UPDATE
     SET xoePending = true
  */               
    """)
    @ReplicationRunOnChange([XObjectEntity::class])
    @ReplicationCheckPendingNotificationsFor([XObjectEntity::class])
    abstract suspend fun replicateOnChange()

    @JsName("findByObjectId")
    @Query("SELECT * from XObjectEntity WHERE objectId = :id")
    abstract fun findByObjectId(id: String?): XObjectEntity?

    @JsName("findByXobjectUid")
    @Query("SELECT * from XObjectEntity WHERE xObjectUid = :xObjectUid")
    abstract fun findByXobjectUid(xObjectUid: Long): XObjectEntity?

}
