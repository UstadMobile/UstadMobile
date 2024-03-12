package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import androidx.room.Update
import app.cash.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.composites.DiscussionPostAndPosterNames
import com.ustadmobile.lib.db.composites.PersonAndPicture
import com.ustadmobile.lib.db.entities.*

@DoorDao
@Repository
expect abstract class DiscussionPostDao: BaseDao<DiscussionPost>{

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            //Get the top level posts themselves
            HttpServerFunctionCall(
                functionName = "getTopLevelPostsByCourseBlockUid",
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "includeDeleted",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true"
                    ),
                )
            ),
            //Get the person entity associated with the post
            HttpServerFunctionCall("getTopLevelPostsByCourseBlockUidPersons"),
            //Get the most recent reply.
            HttpServerFunctionCall("getTopLevelPostsByCourseBlockUidLatestMessage"),
        )
    )
    @Query("""
        SELECT DiscussionPost.*,
               Person.firstNames as authorPersonFirstNames,
               Person.lastName as authorPersonLastName,
               PersonPicture.personPictureThumbnailUri AS authorPictureUri,
               MostRecentReply.discussionPostMessage AS postLatestMessage,
               COALESCE(MostRecentReply.discussionPostStartDate, 0) AS postLatestMessageTimestamp,
               (SELECT COUNT(*)
                  FROM DiscussionPost DiscussionPostReplies
                 WHERE DiscussionPostReplies.discussionPostReplyToPostUid = DiscussionPost.discussionPostUid
                   AND NOT DiscussionPostReplies.dpDeleted
               ) AS postRepliesCount
          FROM DiscussionPost
               LEFT JOIN DiscussionPost AS MostRecentReply
                         ON MostRecentReply.discussionPostUid = 
                            (SELECT MostRecentReplyInner.discussionPostUid
                               FROM DiscussionPost AS MostRecentReplyInner
                              WHERE MostRecentReplyInner.discussionPostReplyToPostUid = DiscussionPost.discussionPostUid
                           ORDER BY MostRecentReplyInner.discussionPostStartDate DESC
                              LIMIT 1  
                            )
               LEFT JOIN Person 
                         ON Person.personUid = DiscussionPost.discussionPostStartedPersonUid
               LEFT JOIN PersonPicture
                         ON PersonPicture.personPictureUid = DiscussionPost.discussionPostStartedPersonUid
         WHERE DiscussionPost.discussionPostCourseBlockUid = :courseBlockUid
           AND DiscussionPost.discussionPostReplyToPostUid = 0
           AND (NOT DiscussionPost.dpDeleted OR CAST(:includeDeleted AS INTEGER) = 1)
      ORDER BY DiscussionPost.discussionPostStartDate DESC          
    """)
    abstract fun getTopLevelPostsByCourseBlockUid(
        courseBlockUid: Long,
        includeDeleted: Boolean,
    ): PagingSource<Int, DiscussionPostWithDetails>

    @Query("""
        SELECT Person.*, PersonPicture.*
          FROM Person
               LEFT JOIN PersonPicture
                         ON PersonPicture.personPictureUid = Person.personUid
         WHERE Person.personUid IN
               (SELECT DISTINCT DiscussionPost.discussionPostStartedPersonUid
                  FROM DiscussionPost
                 WHERE DiscussionPost.discussionPostCourseBlockUid = :courseBlockUid
                   AND DiscussionPost.discussionPostReplyToPostUid = 0)
    """)
    abstract suspend fun getTopLevelPostsByCourseBlockUidPersons(
        courseBlockUid: Long
    ): List<PersonAndPicture>

    @Query("""
        SELECT MostRecentReply.*
          FROM DiscussionPost
               JOIN DiscussionPost AS MostRecentReply
                         ON MostRecentReply.discussionPostUid = 
                            (SELECT MostRecentReplyInner.discussionPostUid
                               FROM DiscussionPost AS MostRecentReplyInner
                              WHERE MostRecentReplyInner.discussionPostReplyToPostUid = DiscussionPost.discussionPostUid
                           ORDER BY MostRecentReplyInner.discussionPostStartDate DESC
                              LIMIT 1  
                            )
         WHERE DiscussionPost.discussionPostCourseBlockUid = :courseBlockUid
           AND DiscussionPost.discussionPostReplyToPostUid = 0 
    """)
    abstract suspend fun getTopLevelPostsByCourseBlockUidLatestMessage(
        courseBlockUid: Long
    ): List<DiscussionPost>

    @Query("""
        SELECT DiscussionPost.discussionPostTitle 
          FROM DiscussionPost 
         WHERE DiscussionPost.discussionPostUid = :postUid
    """)
    abstract suspend fun getPostTitle(postUid: Long): String?

    @Query("""
        SELECT * 
         FROM DiscussionPost
        WHERE DiscussionPost.discussionPostUid = :uid
    """)
    abstract suspend fun findByUid(uid: Long): DiscussionPost?

    @Query("""
        SELECT DiscussionPost.discussionPostTitle
          FROM DiscussionPost
         WHERE DiscussionPost.discussionPostUid = :uid
    """)
    abstract fun getTitleByUidAsFlow(uid: Long): Flow<String?>

    @Update
    abstract suspend fun updateAsync(entity: DiscussionPost): Int


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findByPostIdWithAllReplies",
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "includeDeleted",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true",
                    )
                )
            ),
            HttpServerFunctionCall("findByPostIdWithAllRepliesPersons"),
        )
    )
    @Query("""
        SELECT DiscussionPost.*,
               Person.firstNames,
               Person.lastName,
               PersonPicture.personPictureThumbnailUri AS personPictureUri
          FROM DiscussionPost
               LEFT JOIN Person
                         ON Person.personUid = DiscussionPost.discussionPostStartedPersonUid
               LEFT JOIN PersonPicture
                         ON PersonPicture.personPictureUid = DiscussionPost.discussionPostStartedPersonUid
         WHERE (DiscussionPost.discussionPostUid = :postUid
                 OR DiscussionPost.discussionPostReplyToPostUid= :postUid)
           AND (NOT DiscussionPost.dpDeleted OR CAST(:includeDeleted AS INTEGER) = 1)      
            -- Always get the starting post first, followed by replies
      ORDER BY CASE(DiscussionPost.discussionPostReplyToPostUid)
               WHEN 0 THEN 0
               ELSE 1 END ASC,
               DiscussionPost.discussionPostStartDate DESC 
    """)
    abstract fun findByPostIdWithAllReplies(
        postUid: Long,
        includeDeleted: Boolean,
    ): PagingSource<Int, DiscussionPostAndPosterNames>

    @Query("""
        SELECT Person.*, PersonPicture.*
          FROM Person
               LEFT JOIN PersonPicture
                         ON PersonPicture.personPictureUid = Person.personUid
         WHERE Person.personUid IN
               (SELECT DISTINCT DiscussionPost.discussionPostStartedPersonUid
                  FROM DiscussionPost
                 WHERE DiscussionPost.discussionPostUid = :postUid
                    OR DiscussionPost.discussionPostReplyToPostUid= :postUid)
    """)
    abstract suspend fun findByPostIdWithAllRepliesPersons(
        postUid: Long
    ): List<PersonAndPicture>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(
        entity: DiscussionPost
    )

    @Query("""
        UPDATE DiscussionPost
           SET dpDeleted = :deleted,
               discussionPostLct = :updateTime
         WHERE discussionPostUid = :uid   
    """)
    abstract suspend fun setDeletedAsync(
        uid: Long,
        deleted: Boolean,
        updateTime: Long
    )

}