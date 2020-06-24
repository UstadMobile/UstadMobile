package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.Comments
import com.ustadmobile.lib.db.entities.CommentsWithPerson

@UmDao
@UmRepository
@Dao
abstract class CommentsDao : BaseDao<Comments>, OneToManyJoinDao<Comments> {

    @Query("SELECT * FROM Comments WHERE commentsUid = :uid " +
            " AND CAST(commentsInActive AS INTEGER) = 0")
    abstract fun findByUidAsync(uid: Long): Comments?

    @Query("""
        SELECT Comments.*, Person.* FROM Comments
        LEFT JOIN Person ON Person.personUid = Comments. commentsPersonUid 
        WHERE Comments.commentsEntityType = :entityType 
        AND Comments.commentsEntityUid = :entityUid
        AND CAST(Comments.commentsFlagged AS INTEGER) = 0
        AND CAST(Comments.commentsInActive AS INTEGER) = 0
        AND CAST(Comments.commentsPublic AS INTEGER) = 1
        ORDER BY Comments.commentsDateTimeAdded DESC 
    """)
    abstract fun findPublicByEntityTypeAndUidLive(entityType: Int, entityUid: Long):
            DataSource.Factory<Int, CommentsWithPerson>

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
            DataSource.Factory<Int, CommentsWithPerson>


    @Query("""
        SELECT Comments.*, Person.* FROM Comments
        LEFT JOIN Person ON Person.personUid = Comments.commentsPersonUid 
        WHERE Comments.commentsEntityType = :entityType 
        AND Comments.commentsEntityUid = :entityUid
        AND CAST(Comments.commentsFlagged AS INTEGER) = 0
        AND CAST(Comments.commentsInActive AS INTEGER) = 0
        AND CAST(Comments.commentsPublic AS INTEGER) = 0
        AND Comments.commentsPersonUid = :personFrom AND Comments.commentsToPersonUid = :personTo
        ORDER BY Comments.commentsDateTimeAdded DESC 
    """)
    abstract fun findPrivateCommentsByEntityTypeAndUidAndPersonAndPersonToLive(
            entityType: Int, entityUid: Long, personFrom: Long, personTo: Long):
            DataSource.Factory<Int, CommentsWithPerson>

    @Query("""
        SELECT Comments.*, Person.* FROM Comments
        LEFT JOIN Person ON Person.personUid = Comments.commentsPersonUid 
        WHERE Comments.commentsEntityType = :entityType 
        AND Comments.commentsEntityUid = :entityUid
        AND CAST(Comments.commentsFlagged AS INTEGER) = 0
        AND CAST(Comments.commentsInActive AS INTEGER) = 0
        AND CAST(Comments.commentsPublic AS INTEGER) = 0
        AND Comments.commentsPersonUid = :personFrom AND Comments.commentsToPersonUid = :personTo
        ORDER BY Comments.commentsDateTimeAdded DESC 
    """)
    abstract fun findPrivateCommentsByEntityTypeAndUidAndPersonAndPersonToTest(
            entityType: Int, entityUid: Long, personFrom: Long, personTo: Long):
            List<CommentsWithPerson>

    @Query("""
        UPDATE Comments SET commentsInActive = :inActive WHERE 
        Comments.commentsUid = :uid
    """)
    abstract suspend fun updateInActiveByCommentUid(uid: Long, inActive: Boolean)

    override suspend fun deactivateByUids(uidList: List<Long>) {
        uidList.forEach {
            updateInActiveByCommentUid(it, true)
        }
    }
}
