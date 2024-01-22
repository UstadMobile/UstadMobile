package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import androidx.room.Update
import app.cash.paging.PagingSource
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.CourseTerminology

@Repository
@DoorDao
expect abstract class CourseTerminologyDao : BaseDao<CourseTerminology> {


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""
        SELECT *
         FROM CourseTerminology
     ORDER BY ctTitle   
    """)
    abstract fun findAllCourseTerminologyPagingSource(): PagingSource<Int, CourseTerminology>

    @Query("""
        SELECT *
         FROM CourseTerminology
     ORDER BY ctTitle   
    """)
    abstract fun findAllCourseTerminologyList(): List<CourseTerminology>


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""
        SELECT *
          FROM CourseTerminology
               JOIN Clazz 
               ON Clazz.clazzTerminologyUid = CourseTerminology.ctUid
         WHERE Clazz.clazzUid = :clazzUid
    """)
    abstract suspend fun getTerminologyForClazz(clazzUid: Long): CourseTerminology?


    @Query("""
        SELECT CourseTerminology.*
          FROM ClazzAssignment
               JOIN Clazz 
                    ON Clazz.clazzUid = ClazzAssignment.caClazzUid
               JOIN CourseTerminology
                    ON CourseTerminology.ctUid = Clazz.clazzTerminologyUid
         WHERE ClazzAssignment.caUid = :assignmentUid 
         LIMIT 1
    """)
    abstract suspend fun getTerminologyForAssignment(assignmentUid: Long): CourseTerminology?

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""
        SELECT * 
         FROM CourseTerminology 
        WHERE ctUid = :uid
        """)
    abstract suspend fun findByUidAsync(uid: Long): CourseTerminology?

    @Update
    abstract suspend fun updateAsync(entity: CourseTerminology): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(entity: CourseTerminology): Long

}