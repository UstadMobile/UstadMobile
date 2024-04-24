package com.ustadmobile.core.db.dao


import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import app.cash.paging.PagingSource
import com.ustadmobile.core.db.UNSET_DISTANT_FUTURE
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.HttpServerFunctionCall
import com.ustadmobile.door.annotation.HttpServerFunctionParam
import com.ustadmobile.lib.db.composites.CourseBlockAndDisplayDetails
import com.ustadmobile.lib.db.composites.CourseBlockAndDbEntities
import com.ustadmobile.lib.db.composites.CourseBlockUidAndClazzUid
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.flow.Flow

@Repository
@DoorDao
expect abstract class CourseBlockDao : BaseDao<CourseBlock>, OneToManyJoinDao<CourseBlock> {

    @Query("SELECT * FROM CourseBlock WHERE cbUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): CourseBlock?

    @Update
    abstract suspend fun updateAsync(entity: CourseBlock): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceListAsync(list: List<CourseBlock>)


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("SELECT * FROM CourseBlock WHERE cbUid = :uid")
    abstract fun findByUidAsyncAsFlow(uid: Long): Flow<CourseBlock?>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findAllCourseBlockByClazzUidAsync",
                functionArgs = arrayOf(
                    //Include inactive when fetching from server to ensure local db gets updated
                    HttpServerFunctionParam(
                        name = "includeInactive",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true"
                    )
                )
            )
        )
    )
    @Query("""
        SELECT CourseBlock.*, Assignment.*, Entry.*, Language.*, CourseBlockPicture.*,
               (SELECT CourseGroupSet.cgsName
                  FROM CourseGroupSet
                 WHERE CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
                   AND assignment.caGroupUid != 0
                   AND CourseGroupSet.cgsUid = assignment.caGroupUid) AS assignmentCourseGroupSetName
          FROM CourseBlock 
               LEFT JOIN ClazzAssignment AS Assignment
                         ON assignment.caUid = CourseBlock.cbEntityUid
                            AND CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
               LEFT JOIN ContentEntry AS Entry
                         ON entry.contentEntryUid = CourseBlock.cbEntityUid
                            AND CourseBlock.cbType = ${CourseBlock.BLOCK_CONTENT_TYPE}
               LEFT JOIN Language
                         ON Language.langUid = Entry.primaryLanguageUid
                            AND CourseBlock.cbType = ${CourseBlock.BLOCK_CONTENT_TYPE}
               LEFT JOIN CourseBlockPicture
                         ON CourseBlockPicture.cbpUid = CourseBlock.cbUid    
         WHERE CourseBlock.cbClazzUid = :clazzUid
           AND (CAST(:includeInactive AS INTEGER) = 1 OR CourseBlock.cbActive)
      ORDER BY CourseBlock.cbIndex
          """)
    abstract suspend fun findAllCourseBlockByClazzUidAsync(
        clazzUid: Long,
        includeInactive: Boolean,
    ): List<CourseBlockAndDbEntities>



    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findAllCourseBlockByClazzUidAsPagingSource",
                functionArgs = arrayOf(
                    //When pulling over HTTP include inactive entities to ensure it gets updated on client db
                    HttpServerFunctionParam(
                        name = "includeInactive",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true",
                    ),
                    HttpServerFunctionParam(
                        name = "includeHidden",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true",
                    ),
                    HttpServerFunctionParam(
                        name = "hideUntilFilterTime",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "${UNSET_DISTANT_FUTURE}L",
                    )
                )
            )
        )
    )
    @Query("""
        SELECT CourseBlock.*, ContentEntry.*, CourseBlockPicture.*,
               CourseBlock.cbUid NOT IN(:collapseList) AS expanded
          FROM CourseBlock
               LEFT JOIN ContentEntry
                         ON CourseBlock.cbType = ${CourseBlock.BLOCK_CONTENT_TYPE}
                            AND ContentEntry.contentEntryUid = CourseBlock.cbEntityUid
               LEFT JOIN CourseBlockPicture
                         ON CourseBlockPicture.cbpUid = CourseBlock.cbUid    
         WHERE CourseBlock.cbClazzUid = :clazzUid
           AND CourseBlock.cbModuleParentBlockUid NOT IN(:collapseList)
           AND (CAST(:includeInactive AS INTEGER) = 1 OR CourseBlock.cbActive)
           AND (CAST(:includeHidden AS INTEGER) = 1 OR NOT CourseBlock.cbHidden)
           AND (:hideUntilFilterTime >= CourseBlock.cbHideUntilDate)
           AND (:hideUntilFilterTime >= COALESCE(
                (SELECT CourseBlockParent.cbHideUntilDate
                   FROM CourseBlock CourseBlockParent
                  WHERE CourseBlockParent.cbUid = CourseBlock.cbModuleParentBlockUid), 0))
      ORDER BY CourseBlock.cbIndex       
    """)
    abstract fun findAllCourseBlockByClazzUidAsPagingSource(
        clazzUid: Long,
        collapseList: List<Long>,
        includeInactive: Boolean,
        includeHidden: Boolean,
        hideUntilFilterTime: Long,
    ): PagingSource<Int, CourseBlockAndDisplayDetails>

    @Query("""
        UPDATE CourseBlock 
           SET cbActive = :active, 
               cbLct = :changeTime
         WHERE cbUid = :cbUid""")
    abstract suspend fun updateActiveByUid(cbUid: Long, active: Boolean,  changeTime: Long)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertListAsync(entities: List<CourseBlock>)

    @Query("""
        SELECT CourseBlock.cbTitle
          FROM CourseBlock 
         WHERE CourseBlock.cbEntityUid = :assignmentUid
           AND CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
    """)
    abstract fun getTitleByAssignmentUid(assignmentUid: Long) : Flow<String?>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""
        SELECT CourseBlock.*
          FROM CourseBlock
         WHERE CourseBlock.cbUid = :courseBlockUid 
    """)
    abstract fun findByUidAsFlow(courseBlockUid: Long): Flow<CourseBlock?>


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall("findCourseBlockByDiscussionPostUid")
        )
    )
    @Query("""
        SELECT COALESCE(CourseBlock.cbUid, 0) AS courseBlockUid,
               COALESCE(CourseBlock.cbClazzUid, 0) AS clazzUid
          FROM CourseBlock
         WHERE CourseBlock.cbUid = 
               (SELECT DiscussionPost.discussionPostCourseBlockUid 
                  FROM DiscussionPost
                 WHERE DiscussionPost.discussionPostUid = :postUid)
         LIMIT 1
    """)
    abstract suspend fun findCourseBlockAndClazzUidByDiscussionPostUid(
        postUid: Long
    ): CourseBlockUidAndClazzUid?

    @Query("""
        SELECT CourseBlock.*
          FROM CourseBlock
         WHERE CourseBlock.cbUid = 
               (SELECT DiscussionPost.discussionPostCourseBlockUid 
                  FROM DiscussionPost
                 WHERE DiscussionPost.discussionPostUid = :postUid) 
    """)
    abstract suspend fun findCourseBlockByDiscussionPostUid(
        postUid: Long
    ): CourseBlock?

    @Query("""
        SELECT COALESCE(CourseBlock.cbClazzUid, 0) AS clazzUid
          FROM CourseBlock
         WHERE CourseBlock.cbUid = :courseBlockUid
    """)
    abstract suspend fun findClazzUidByCourseBlockUid(
        courseBlockUid: Long
    ): Long

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
    )
    @Query("""
        SELECT CourseBlock.*
          FROM CourseBlock
         WHERE CourseBlock.cbEntityUid = :assignmentUid
           AND CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
         LIMIT 1 
    """)
    abstract fun findCourseBlockByAssignmentUid(
        assignmentUid: Long
    ): Flow<CourseBlock?>
}