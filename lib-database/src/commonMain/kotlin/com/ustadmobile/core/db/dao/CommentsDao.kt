package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Comments
import com.ustadmobile.lib.db.entities.CommentsWithPerson
import com.ustadmobile.lib.db.entities.UserSession

@Repository
@Dao
abstract class CommentsDao : BaseDao<Comments>, OneToManyJoinDao<Comments> {

    @Query("""
     REPLACE INTO CommentsReplicate(commentsPk, commentsDestination)
      SELECT DISTINCT Comments.commentsUid AS commentsPk,
             :newNodeId AS commentsDestination
        FROM Comments
       WHERE Comments.commentsLct != COALESCE(
             (SELECT commentsVersionId
                FROM CommentsReplicate
               WHERE commentsPk = Comments.commentsUid
                 AND commentsDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(commentsPk, commentsDestination) DO UPDATE
             SET commentsPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([Comments::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
     REPLACE INTO CommentsReplicate(commentsPk, commentsDestination)
      SELECT DISTINCT Comments.commentsUid AS commentsPk,
             UserSession.usClientNodeId AS commentsDestination
        FROM ChangeLog
             JOIN Comments
                 ON ChangeLog.chTableId = ${Comments.TABLE_ID}
                    AND ChangeLog.chEntityPk = Comments.commentsUid
             JOIN UserSession 
                  ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
       WHERE UserSession.usClientNodeId != (
             SELECT nodeClientId 
               FROM SyncNode
              LIMIT 1)
         AND Comments.commentsLct != COALESCE(
             (SELECT commentsVersionId
                FROM CommentsReplicate
               WHERE commentsPk = Comments.commentsUid
                 AND commentsDestination = UserSession.usClientNodeId), 0)
     /*psql ON CONFLICT(commentsPk, commentsDestination) DO UPDATE
         SET commentsPending = true
      */               
    """)
    @ReplicationRunOnChange([Comments::class])
    @ReplicationCheckPendingNotificationsFor([Comments::class])
    abstract suspend fun replicateOnChange()

    @Query("SELECT * FROM Comments WHERE commentsUid = :uid " +
            " AND CAST(commentsInActive AS INTEGER) = 0")
    abstract fun findByUidAsync(uid: Long): Comments?

    @Query("""
        SELECT Comments.*, Person.* 
          FROM Comments
                LEFT JOIN Person 
                ON Person.personUid = Comments.commentsPersonUid 
         WHERE Comments.commentsEntityType = :entityType 
           AND Comments.commentsEntityUid = :entityUid
           AND CAST(Comments.commentsFlagged AS INTEGER) = 0
           AND CAST(Comments.commentsInActive AS INTEGER) = 0
           AND CAST(Comments.commentsPublic AS INTEGER) = 1
      ORDER BY Comments.commentsDateTimeAdded DESC 
    """)
    abstract fun findPublicByEntityTypeAndUidLive(entityType: Int, entityUid: Long):
            DoorDataSourceFactory<Int, CommentsWithPerson>


    @Query("""
        SELECT Comments.*, Person.* FROM Comments
        LEFT JOIN Person ON Person.personUid = Comments.commentsPersonUid 
        WHERE Comments.commentsEntityType = :entityType 
        AND Comments.commentsEntityUid = :entityUid
        AND Comments.commentsPersonUid = :personUid OR Comments.commentsToPersonUid = :personUid 
        AND CAST(Comments.commentsFlagged AS INTEGER) = 0
        AND CAST(Comments.commentsInActive AS INTEGER) = 0
        AND CAST(Comments.commentsPublic AS INTEGER) = 0
        AND Person.personUid = :personUid
        ORDER BY Comments.commentsDateTimeAdded DESC 
    """)
    abstract fun findPrivateByEntityTypeAndUidAndForPersonLive(entityType: Int, entityUid: Long,
                                                            personUid: Long):
            DoorDataSourceFactory<Int, CommentsWithPerson>


    @Query("""
            SELECT Comments.*, 
                   Person.* 
              FROM Comments
                   LEFT JOIN Person 
                   ON Person.personUid = Comments.commentsPersonUid
             WHERE Comments.commentsEntityType = :entityType 
               AND Comments.commentsEntityUid = :entityUid
               AND Comments.commentSubmitterUid = :submitterUid  
               AND CAST(Comments.commentsFlagged AS INTEGER) = 0
               AND CAST(Comments.commentsInActive AS INTEGER) = 0
               AND CAST(Comments.commentsPublic AS INTEGER) = 0
          ORDER BY Comments.commentsDateTimeAdded DESC 
    """)
    abstract fun findPrivateByEntityTypeAndUidAndForPersonLive2(
        entityType: Int,
        entityUid: Long,
        submitterUid: Long
    ):
            DoorDataSourceFactory<Int, CommentsWithPerson>

    @Query("""
        SELECT Comments.*, Person.* FROM Comments
        LEFT JOIN Person ON Person.personUid = Comments.commentsPersonUid 
        WHERE Comments.commentsEntityType = :entityType 
        AND Comments.commentsEntityUid = :entityUid
        AND CAST(Comments.commentsFlagged AS INTEGER) = 0
        AND CAST(Comments.commentsInActive AS INTEGER) = 0
        AND CAST(Comments.commentsPublic AS INTEGER) = 0
        AND Person.personUid = :personUid
        ORDER BY Comments.commentsDateTimeAdded DESC 
    """)
    abstract fun findPrivateByEntityTypeAndUidAndPersonLive(entityType: Int, entityUid: Long,
                                                                personUid: Long):
            DoorDataSourceFactory<Int, CommentsWithPerson>


    /*
       SELECT Comments.*, Person.* FROM Comments
        LEFT JOIN Person ON Person.personUid = Comments.commentsPersonUid
        WHERE Comments.commentsEntityType = :entityType
        AND Comments.commentsEntityUid = :entityUid
        AND CAST(Comments.commentsFlagged AS INTEGER) = 0
        AND CAST(Comments.commentsInActive AS INTEGER) = 0
        AND CAST(Comments.commentsPublic AS INTEGER) = 0
        AND Comments.commentsPersonUid = :personFrom
        OR (:personTo = 0 OR Comments.commentsToPersonUid = :personFrom)
        ORDER BY Comments.commentsDateTimeAdded DESC
     */
    @Query("""
        SELECT Comments.*, Person.* FROM Comments
        LEFT JOIN Person ON Person.personUid = Comments.commentsPersonUid 
        WHERE Comments.commentsEntityType = :entityType 
        AND Comments.commentsEntityUid = :entityUid
        AND CAST(Comments.commentsFlagged AS INTEGER) = 0
        AND CAST(Comments.commentsInActive AS INTEGER) = 0
        AND CAST(Comments.commentsPublic AS INTEGER) = 0
        AND (Comments.commentsToPersonUid = :personFrom 
         OR Comments.commentsPersonUid = :personFrom)
        ORDER BY Comments.commentsDateTimeAdded DESC 
    """)
    abstract fun findPrivateCommentsByEntityTypeAndUidAndPersonAndPersonToLive(
            entityType: Int, entityUid: Long, personFrom: Long):
            DoorDataSourceFactory<Int, CommentsWithPerson>

    @Query("""
        SELECT Comments.*, Person.* FROM Comments
        LEFT JOIN Person ON Person.personUid = Comments.commentsPersonUid 
        WHERE Comments.commentsEntityType = :entityType 
        AND Comments.commentsEntityUid = :entityUid
        AND CAST(Comments.commentsFlagged AS INTEGER) = 0
        AND CAST(Comments.commentsInActive AS INTEGER) = 0
        AND CAST(Comments.commentsPublic AS INTEGER) = 0
        AND Comments.commentsPersonUid = :personFrom 
        OR (:personTo = 0 OR Comments.commentsToPersonUid = :personFrom)
        ORDER BY Comments.commentsDateTimeAdded DESC 
    """)
    abstract fun findPrivateCommentsByEntityTypeAndUidAndPersonAndPersonToTest(
            entityType: Int, entityUid: Long, personFrom: Long, personTo: Long):
            List<CommentsWithPerson>

    @Query("""
        UPDATE Comments 
           SET commentsInActive = :inActive,
               commentsLct = :changeTime
         WHERE Comments.commentsUid = :uid
    """)
    abstract suspend fun updateInActiveByCommentUid(
        uid: Long,
        inActive: Boolean,
        changeTime: Long
    )

    override suspend fun deactivateByUids(uidList: List<Long>, changeTime: Long) {
        uidList.forEach {
            updateInActiveByCommentUid(it, true, changeTime)
        }
    }
}
