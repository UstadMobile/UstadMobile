package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ContextXObjectStatementJoin
import com.ustadmobile.lib.db.entities.UserSession

@Dao
@Repository
abstract class ContextXObjectStatementJoinDao : BaseDao<ContextXObjectStatementJoin> {

    @Query("""
    REPLACE INTO ContextXObjectStatementJoinReplicate(cxosjPk, cxosjDestination)
    SELECT DISTINCT ContextXObjectStatementJoin.contextXObjectStatementJoinUid AS cxosjPk,
         :newNodeId AS cxosjDestination
    FROM ContextXObjectStatementJoin
         LEFT JOIN DoorNode 
              ON DoorNode.nodeId = :newNodeId
    --notpsql
    WHERE ContextXObjectStatementJoin.contextXObjectLct != COALESCE(
         (SELECT cxosjVersionId
            FROM ContextXObjectStatementJoinReplicate
           WHERE cxosjPk = ContextXObjectStatementJoin.contextXObjectStatementJoinUid
             AND cxosjDestination = DoorNode.nodeId), 0) 
    --endnotpsql         
    /*psql ON CONFLICT(cxosjPk, cxosjDestination) DO UPDATE
     SET cxosjPending = (SELECT ContextXObjectStatementJoin.contextXObjectLct
                           FROM ContextXObjectStatementJoin
                          WHERE ContextXObjectStatementJoin.contextXObjectStatementJoinUid = EXCLUDED.cxosjPk ) 
                                != ContextXObjectStatementJoinReplicate.cxosjVersionId             
    */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([ContextXObjectStatementJoin::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
    REPLACE INTO ContextXObjectStatementJoinReplicate(cxosjPk, cxosjDestination)
    SELECT DISTINCT ContextXObjectStatementJoin.contextXObjectStatementJoinUid AS cxosjUid,
         UserSession.usClientNodeId AS cxosjDestination
    FROM ChangeLog
         JOIN ContextXObjectStatementJoin
             ON ChangeLog.chTableId = ${ContextXObjectStatementJoin.TABLE_ID}
                AND ChangeLog.chEntityPk = ContextXObjectStatementJoin.contextXObjectStatementJoinUid
         JOIN UserSession ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
    WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
   --notpsql       
     AND ContextXObjectStatementJoin.contextXObjectLct != COALESCE(
         (SELECT cxosjVersionId
            FROM ContextXObjectStatementJoinReplicate
           WHERE cxosjPk = ContextXObjectStatementJoin.contextXObjectStatementJoinUid
             AND cxosjDestination = UserSession.usClientNodeId), 0)
    --endnotpsql
    /*psql ON CONFLICT(cxosjPk, cxosjDestination) DO UPDATE
     SET cxosjPending = (SELECT ContextXObjectStatementJoin.contextXObjectLct
                           FROM ContextXObjectStatementJoin
                          WHERE ContextXObjectStatementJoin.contextXObjectStatementJoinUid = EXCLUDED.cxosjPk ) 
                                != ContextXObjectStatementJoinReplicate.cxosjVersionId             
    */               
    """)
    @ReplicationRunOnChange([ContextXObjectStatementJoin::class])
    @ReplicationCheckPendingNotificationsFor([ContextXObjectStatementJoin::class])
    abstract suspend fun replicateOnChange()

    @Query("SELECT * FROM ContextXObjectStatementJoin where contextStatementUid = :statementUid and contextXObjectUid = :objectUid")
    abstract fun findByStatementAndObjectUid(statementUid: Long, objectUid: Long): ContextXObjectStatementJoin?

    companion object {

        const val CONTEXT_FLAG_PARENT = 0

        const val CONTEXT_FLAG_CATEGORY = 1

        const val CONTEXT_FLAG_GROUPING = 2

        const val CONTEXT_FLAG_OTHER = 3
    }

}
