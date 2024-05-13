package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.HttpServerFunctionCall
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.composites.StudentResultAndCourseBlockSourcedId
import com.ustadmobile.lib.db.entities.StudentResult

@DoorDao
@Repository
expect abstract class StudentResultDao {

    @Insert
    abstract suspend fun insertListAsync(list: List<StudentResult>)

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall("findByClazzAndStudent"),
            HttpServerFunctionCall(
                functionName = "findByClazzUid",
                functionDao = CourseBlockDao::class,
            )
        )
    )
    @Query("""
        SELECT StudentResult.*,
               CourseBlock.cbSourcedId AS cbSourcedId
          FROM StudentResult
               LEFT JOIN CourseBlock
                         ON StudentResult.srCourseBlockUid = CourseBlock.cbUid 
         WHERE StudentResult.srClazzUid = :clazzUid
           AND StudentResult.srStudentPersonUid = :studentPersonUid
           AND :accountPersonUid = :accountPersonUid
    """)
    abstract suspend fun findByClazzAndStudent(
        clazzUid: Long,
        studentPersonUid: Long,
        accountPersonUid: Long
    ): List<StudentResultAndCourseBlockSourcedId>

    @Query("""
        SELECT COALESCE(
               (SELECT StudentResult.srUid  
                  FROM StudentResult 
                 WHERE srSourcedId = :sourcedId), 0)
    """)
    abstract suspend fun findUidBySourcedId(sourcedId: String): Long

    @Update
    abstract suspend fun updateAsync(studentResult: StudentResult)


}