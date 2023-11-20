package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon.SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL
import app.cash.paging.PagingSource
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.lib.db.entities.Comments
import com.ustadmobile.lib.db.entities.CommentsWithPerson
import com.ustadmobile.lib.db.entities.Person

@Repository
@DoorDao
expect abstract class CommentsDao : BaseDao<Comments>, OneToManyJoinDao<Comments> {

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
            PagingSource<Int, CommentsWithPerson>


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
            PagingSource<Int, CommentsWithPerson>


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
            PagingSource<Int, CommentsWithPerson>

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
            PagingSource<Int, CommentsWithPerson>


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


    //Requires the Comment, Person for comment, ClazzEnrolment and CourseGroupMember (to determine
    // the submitter id for the given accountPersonUid).
    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(functionName = "findPrivateCommentsForUserByAssignmentUid"),
            HttpServerFunctionCall(functionName = "findPrivateCommentsForUserByAssignmentUidPersons"),
            HttpServerFunctionCall(
                functionDao = ClazzAssignmentDao::class,
                functionName = "findEnrolmentsByPersonUidAndAssignmentUid"
            ),
            HttpServerFunctionCall(
                functionDao = ClazzAssignmentDao::class,
                functionName = "findCourseGroupMembersByPersonUidAndAssignmentUid"
            )
        )
    )
    @Query("""
        SELECT Comments.*,
               Person.firstNames AS firstNames, 
               Person.lastName AS lastName
          FROM Comments
               LEFT JOIN Person 
                    ON Person.personUid = Comments.commentsPersonUid
         WHERE Comments.commentSubmitterUid = ($SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL)
           AND Comments.commentSubmitterUid != 0
           AND Comments.commentsEntityUid = :assignmentUid
           AND CAST(Comments.commentsInActive AS INTEGER) = 0
      ORDER BY Comments.commentsDateTimeAdded DESC     
    """)
    abstract fun findPrivateCommentsForUserByAssignmentUid(
        accountPersonUid: Long,
        assignmentUid: Long,
    ): PagingSource<Int, CommentsAndName>

    @Query("""
        SELECT Person.*
          FROM Person
         WHERE Person.personUid IN
               (SELECT DISTINCT Comments.commentsPersonUid
                  FROM Comments
                 WHERE Comments.commentSubmitterUid = ($SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL)
                   AND Comments.commentSubmitterUid != 0
                   AND Comments.commentsEntityUid = :assignmentUid
                   AND CAST(Comments.commentsInActive AS INTEGER) = 0)
    """)
    abstract suspend fun findPrivateCommentsForUserByAssignmentUidPersons(
        accountPersonUid: Long,
        assignmentUid: Long
    ): List<Person>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall("findPrivateCommentsForSubmitterByAssignmentUid"),
            HttpServerFunctionCall("findPrivateCommentsForSubmitterByAssignmentUidPersons"),
            //Needs
        )
    )
    @Query("""
        SELECT Comments.*,
               Person.firstNames AS firstNames, 
               Person.lastName AS lastName
          FROM Comments
               LEFT JOIN Person 
                    ON Person.personUid = Comments.commentsPersonUid
         WHERE Comments.commentSubmitterUid = :submitterUid
           AND Comments.commentsEntityUid = :assignmentUid
           AND NOT Comments.commentsInActive
      ORDER BY Comments.commentsDateTimeAdded DESC        
    """)
    abstract fun findPrivateCommentsForSubmitterByAssignmentUid(
        submitterUid: Long,
        assignmentUid: Long,
    ): PagingSource<Int, CommentsAndName>

    @Query("""
        SELECT Person.*
          FROM Person
         WHERE Person.personUid IN 
               (SELECT Comments.commentsPersonUid
                  FROM Comments
                 WHERE Comments.commentSubmitterUid = :submitterUid
                   AND Comments.commentsEntityUid = :assignmentUid
                   AND NOT Comments.commentsInActive) 
    """
    )
    abstract fun findPrivateCommentsForSubmitterByAssignmentUidPersons(
        submitterUid: Long,
        assignmentUid: Long
    ): List<Person>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(functionName = "findCourseCommentsByAssignmentUid"),
            HttpServerFunctionCall(functionName = "findCourseCommentsByAssignmentUidPersons")
        )
    )
    @Query("""
        SELECT Comments.*,
               Person.firstNames AS firstNames, 
               Person.lastName AS lastName
          FROM Comments
               LEFT JOIN Person 
                    ON Person.personUid = Comments.commentsPersonUid
         WHERE Comments.commentsEntityUid = :assignmentUid
           AND Comments.commentSubmitterUid = 0
      ORDER BY Comments.commentsDateTimeAdded DESC     
    """)
    abstract fun findCourseCommentsByAssignmentUid(
        assignmentUid: Long
    ): PagingSource<Int, CommentsAndName>

    @Query("""
        SELECT Person.*
          FROM Person
         WHERE Person.personUid IN
               (SELECT DISTINCT Comments.commentsPersonUid
                  FROM Comments
                       LEFT JOIN Person 
                            ON Person.personUid = Comments.commentsPersonUid
                 WHERE Comments.commentsEntityUid = :assignmentUid
                   AND Comments.commentSubmitterUid = 0)
    """)
    abstract suspend fun findCourseCommentsByAssignmentUidPersons(
        assignmentUid: Long
    ): List<Person>


}
