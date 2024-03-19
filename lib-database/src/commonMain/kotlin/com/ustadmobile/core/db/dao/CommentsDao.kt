package com.ustadmobile.core.db.dao

import androidx.room.Insert
import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon.SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL
import app.cash.paging.PagingSource
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.lib.db.composites.PersonAndPicture
import com.ustadmobile.lib.db.entities.Comments
import com.ustadmobile.lib.db.entities.Person

@Repository
@DoorDao
expect abstract class CommentsDao  {

    @Insert
    abstract suspend fun insertAsync(comments: Comments): Long

    @Query("""
        UPDATE Comments 
           SET commentsDeleted = :deleted,
               commentsLct = :changeTime
         WHERE Comments.commentsUid = :uid
    """)
    abstract suspend fun updateDeletedByCommentUid(
        uid: Long,
        deleted: Boolean,
        changeTime: Long
    )


    //Requires the Comment, Person for comment, ClazzEnrolment and CourseGroupMember (to determine
    // the submitter id for the given accountPersonUid).
    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findPrivateCommentsForUserByAssignmentUid",
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "includeDeleted",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true",
                    )
                )
            ),
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
               Person.lastName AS lastName,
               PersonPicture.personPictureThumbnailUri AS pictureUri
          FROM Comments
               LEFT JOIN Person 
                    ON Person.personUid = Comments.commentsFromPersonUid
               LEFT JOIN PersonPicture
                    ON PersonPicture.personPictureUid = Comments.commentsFromPersonUid
         WHERE Comments.commentsForSubmitterUid = ($SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL)
           AND Comments.commentsForSubmitterUid != 0
           AND Comments.commentsEntityUid = :assignmentUid
           AND (CAST(Comments.commentsDeleted AS INTEGER) = 0 OR CAST(:includeDeleted AS INTEGER) = 1) 
      ORDER BY Comments.commentsDateTimeAdded DESC     
    """)
    abstract fun findPrivateCommentsForUserByAssignmentUid(
        accountPersonUid: Long,
        assignmentUid: Long,
        includeDeleted: Boolean,
    ): PagingSource<Int, CommentsAndName>

    @Query("""
        SELECT Person.*, PersonPicture.*
          FROM Person
               LEFT JOIN PersonPicture
                         ON PersonPicture.personPictureUid = Person.personUid
         WHERE Person.personUid IN
               (SELECT DISTINCT Comments.commentsFromPersonUid
                  FROM Comments
                 WHERE Comments.commentsForSubmitterUid = ($SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL)
                   AND Comments.commentsForSubmitterUid != 0
                   AND Comments.commentsEntityUid = :assignmentUid
                   AND CAST(Comments.commentsDeleted AS INTEGER) = 0)
    """)
    abstract suspend fun findPrivateCommentsForUserByAssignmentUidPersons(
        accountPersonUid: Long,
        assignmentUid: Long
    ): List<PersonAndPicture>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findPrivateCommentsForSubmitterByAssignmentUid",
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "includeDeleted",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true",
                    )
                )
            ),
            HttpServerFunctionCall("findPrivateCommentsForSubmitterByAssignmentUidPersons"),
            //Needs
        )
    )
    @Query("""
        SELECT Comments.*,
               Person.firstNames AS firstNames, 
               Person.lastName AS lastName,
               PersonPicture.personPictureThumbnailUri AS pictureUri
          FROM Comments
               LEFT JOIN Person 
                    ON Person.personUid = Comments.commentsFromPersonUid
               LEFT JOIN PersonPicture
                    ON PersonPicture.personPictureUid = Comments.commentsFromPersonUid
         WHERE Comments.commentsForSubmitterUid = :submitterUid
           AND Comments.commentsEntityUid = :assignmentUid
           AND (NOT Comments.commentsDeleted OR CAST(:includeDeleted AS INTEGER) = 1)
      ORDER BY Comments.commentsDateTimeAdded DESC        
    """)
    abstract fun findPrivateCommentsForSubmitterByAssignmentUid(
        submitterUid: Long,
        assignmentUid: Long,
        includeDeleted: Boolean,
    ): PagingSource<Int, CommentsAndName>

    @Query("""
        SELECT Person.*, PersonPicture.*
          FROM Person
               LEFT JOIN PersonPicture
                         ON PersonPicture.personPictureUid = Person.personUid 
         WHERE Person.personUid IN 
               (SELECT Comments.commentsFromPersonUid
                  FROM Comments
                 WHERE Comments.commentsForSubmitterUid = :submitterUid
                   AND Comments.commentsEntityUid = :assignmentUid
                   AND NOT Comments.commentsDeleted) 
    """
    )
    abstract fun findPrivateCommentsForSubmitterByAssignmentUidPersons(
        submitterUid: Long,
        assignmentUid: Long
    ): List<PersonAndPicture>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findCourseCommentsByAssignmentUid",
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "includeDeleted",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true",
                    )
                )
            ),
            HttpServerFunctionCall(functionName = "findCourseCommentsByAssignmentUidPersons")
        )
    )
    @Query("""
        SELECT Comments.*,
               Person.firstNames AS firstNames, 
               Person.lastName AS lastName,
               PersonPicture.personPictureThumbnailUri AS pictureUri
          FROM Comments
               LEFT JOIN Person 
                    ON Person.personUid = Comments.commentsFromPersonUid
               LEFT JOIN PersonPicture
                    ON PersonPicture.personPictureUid = Comments.commentsFromPersonUid
         WHERE Comments.commentsEntityUid = :assignmentUid
           AND Comments.commentsForSubmitterUid = 0
           AND (CAST(Comments.commentsDeleted AS INTEGER) = 0 OR CAST(:includeDeleted AS INTEGER) = 1)
      ORDER BY Comments.commentsDateTimeAdded DESC     
    """)
    abstract fun findCourseCommentsByAssignmentUid(
        assignmentUid: Long,
        includeDeleted: Boolean,
    ): PagingSource<Int, CommentsAndName>

    @Query("""
        SELECT Person.*, PersonPicture.*
          FROM Person
               LEFT JOIN PersonPicture
                         ON PersonPicture.personPictureUid = Person.personUid
         WHERE Person.personUid IN
               (SELECT DISTINCT Comments.commentsFromPersonUid
                  FROM Comments
                       LEFT JOIN Person 
                            ON Person.personUid = Comments.commentsFromPersonUid
                 WHERE Comments.commentsEntityUid = :assignmentUid
                   AND Comments.commentsForSubmitterUid = 0)
    """)
    abstract suspend fun findCourseCommentsByAssignmentUidPersons(
        assignmentUid: Long
    ): List<Person>


}
