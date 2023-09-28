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
import com.ustadmobile.lib.db.entities.*

@DoorDao
@Repository
expect abstract class DiscussionPostDao: BaseDao<DiscussionPost>{

    @Query("""
        SELECT DiscussionPost.*,
               Person.firstNames as authorPersonFirstNames,
               Person.lastName as authorPersonLastName,
               MostRecentReply.discussionPostMessage AS postLatestMessage,
               COALESCE(MostRecentReply.discussionPostStartDate, 0) AS postLatestMessageTimestamp,
               (SELECT COUNT(*)
                  FROM DiscussionPost DiscussionPostReplies
                 WHERE DiscussionPostReplies.discussionPostReplyToPostUid = DiscussionPost.discussionPostUid
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
         WHERE DiscussionPost.discussionPostCourseBlockUid = :courseBlockUid
           AND DiscussionPost.discussionPostReplyToPostUid = 0         
      ORDER BY DiscussionPost.discussionPostStartDate DESC          
    """)
    abstract fun getTopLevelPostsByCourseBlockUid(
        courseBlockUid: Long
    ): PagingSource<Int, DiscussionPostWithDetails>

    @Query("""
        SELECT DiscussionPost.discussionPostTitle 
          FROM DiscussionPost 
         WHERE DiscussionPost.discussionPostUid = :postUid
    """)
    abstract suspend fun getPostTitle(postUid: Long): String?

    @Query("""
        SELECT DiscussionPost.discussionPostTitle 
          FROM DiscussionPost 
         WHERE DiscussionPost.discussionPostUid = :postUid
    """)
    abstract fun getPostTitleAsFlow(postUid: Long): Flow<String?>

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

    @Query("""
        SELECT DiscussionPost.*,
            Person.firstNames as authorPersonFirstNames,
            Person.lastName as authorPersonLastName,
            '' AS postLatestMessage,
            0 AS postRepliesCount, 
            DiscussionPost.discussionPostLct AS postLatestMessageTimestamp
             
          FROM DiscussionPost     
          LEFT JOIN Person ON Person.personUid = DiscussionPost.discussionPostStartedPersonUid
         WHERE DiscussionPost.discussionPostUid = :uid
           
    """)
    abstract suspend fun findWithDetailsByUid(uid: Long): DiscussionPostWithDetails?

    @Query("""
        SELECT DiscussionPost.*,
            Person.firstNames as authorPersonFirstNames,
            Person.lastName as authorPersonLastName,
            '' AS postLatestMessage,
            0 AS postRepliesCount, 
            DiscussionPost.discussionPostLct AS postLatestMessageTimestamp
             
          FROM DiscussionPost     
          LEFT JOIN Person ON Person.personUid = DiscussionPost.discussionPostStartedPersonUid
         WHERE DiscussionPost.discussionPostUid = :uid
           
    """)
    abstract fun findWithDetailsByUidAsFlow(uid: Long): Flow<DiscussionPostWithDetails?>

    @Query("""
        SELECT DiscussionPost.*,
            Person.firstNames as authorPersonFirstNames,
            Person.lastName as authorPersonLastName,
            '' AS postLatestMessage,
            0 AS postRepliesCount, 
            DiscussionPost.discussionPostLct AS postLatestMessageTimestamp
             
          FROM DiscussionPost     
          LEFT JOIN Person ON Person.personUid = DiscussionPost.discussionPostStartedPersonUid
         WHERE DiscussionPost.discussionPostUid = :uid
           
    """)
    abstract fun findWithDetailsByUidLive(uid: Long): Flow<DiscussionPostWithDetails?>

    @Update
    abstract suspend fun updateAsync(entity: DiscussionPost): Int



    @Query("""
       SELECT
              DiscussionPost.*,
              Person.*
        FROM DiscussionPost
        LEFT JOIN Person
          ON DiscussionPost.discussionPostStartedPersonUid = Person.personUid
        
       WHERE DiscussionPost.discussionPostReplyToPostUid = :entityUid
              AND CAST(DiscussionPost.discussionPostVisible AS INTEGER) = 1
              AND CAST(DiscussionPost.discussionPostArchive AS INTEGER) = 0
              
    ORDER BY DiscussionPost.discussionPostStartDate DESC
    """)
    abstract fun findAllRepliesByPostUidAsFlow(entityUid: Long):
            Flow<List<DiscussionPostWithPerson>>

    @Query("""
        SELECT DiscussionPost.*,
               Person.firstNames,
               Person.lastName
          FROM DiscussionPost
               LEFT JOIN Person
                         ON Person.personUid = DiscussionPost.discussionPostStartedPersonUid
         WHERE DiscussionPost.discussionPostUid = :postUid
            OR DiscussionPost.discussionPostReplyToPostUid= :postUid
            -- Always get the starting post first, followed by replies
      ORDER BY CASE(DiscussionPost.discussionPostReplyToPostUid)
               WHEN 0 THEN 0
               ELSE 1 END ASC,
               DiscussionPost.discussionPostStartDate DESC 
    """)
    abstract fun findByPostIdWithAllReplies(
        postUid: Long
    ): PagingSource<Int, DiscussionPostAndPosterNames>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(
        entity: DiscussionPost
    )

}