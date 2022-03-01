package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ContextXObjectStatementJoin
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.StatementEntity
import com.ustadmobile.lib.db.entities.UserSession

@Dao
@Repository
abstract class ContextXObjectStatementJoinDao : BaseDao<ContextXObjectStatementJoin> {

    @Query("""
    REPLACE INTO ContextXObjectStatementJoinReplicate(cxosjPk, cxosjDestination)
    SELECT DISTINCT ContextXObjectStatementJoin.contextXObjectStatementJoinUid AS cxosjPk,
         UserSession.usClientNodeId AS cxosjDestination
    FROM UserSession
             JOIN PersonGroupMember
                  ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             JOIN ScopedGrant
                  ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
                     AND (ScopedGrant.sgPermissions & ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT}) > 0
             JOIN StatementEntity
                  ON ${StatementEntity.FROM_SCOPEDGRANT_TO_STATEMENT_JOIN_ON_CLAUSE}
             JOIN ContextXObjectStatementJoin
                  ON ContextXObjectStatementJoin.contextStatementUid = StatementEntity.statementUid
   WHERE UserSession.usClientNodeId = :newNodeId
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
         JOIN StatementEntity
               ON ContextXObjectStatementJoin.contextStatementUid = StatementEntity.statementUid
         JOIN ScopedGrant
              ON ${StatementEntity.FROM_STATEMENT_TO_SCOPEDGRANT_JOIN_ON_CLAUSE}
                 AND (ScopedGrant.sgPermissions & ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT}) > 0
         JOIN PersonGroupMember
              ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
         JOIN UserSession
              ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
                 AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
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
