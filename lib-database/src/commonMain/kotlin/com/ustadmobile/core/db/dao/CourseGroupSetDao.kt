package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.*
import app.cash.paging.PagingSource
import com.ustadmobile.lib.db.entities.CourseGroupSet
import kotlinx.coroutines.flow.Flow

@Repository
@DoorDao
expect abstract class CourseGroupSetDao : BaseDao<CourseGroupSet> {

    @Update
    abstract suspend fun updateAsync(entity: CourseGroupSet): Int

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""
        SELECT *
         FROM CourseGroupSet
        WHERE cgsActive
          AND cgsClazzUid = :clazzUid
          AND ((:searchText = '%') OR (cgsName LIKE :searchText))
     ORDER BY CASE(:sortOrder)
              WHEN ${CourseGroupSetDaoConstants.SORT_NAME_ASC} THEN cgsName
              ELSE ''
              END ASC,
              CASE(:sortOrder)
              WHEN ${CourseGroupSetDaoConstants.SORT_NAME_DESC} THEN cgsName
              ELSE ''
              END DESC
    """)
    abstract fun findAllCourseGroupSetForClazz(
        clazzUid: Long,
        searchText: String,
        sortOrder: Int,
    ): PagingSource<Int, CourseGroupSet>


    @Query("""
        SELECT *
         FROM CourseGroupSet
        WHERE cgsActive
          AND cgsClazzUid = :clazzUid
     ORDER BY cgsName   
    """)
    abstract fun findAllCourseGroupSetForClazzList(clazzUid: Long): List<CourseGroupSet>

    @Query("""
        SELECT *
         FROM CourseGroupSet
        WHERE cgsActive
          AND cgsClazzUid = :clazzUid
     ORDER BY cgsName   
    """)
    abstract suspend fun findAllCourseGroupSetForClazzListAsync(clazzUid: Long): List<CourseGroupSet>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
    )
    @Query("""
        SELECT * 
         FROM CourseGroupSet 
        WHERE cgsUid = :uid
        """)
    abstract suspend fun findByUidAsync(uid: Long): CourseGroupSet?

    @Query("""
        SELECT * 
         FROM CourseGroupSet 
        WHERE cgsUid = :uid
        """)
    abstract fun findByUidAsFlow(uid: Long): Flow<CourseGroupSet?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(entity: CourseGroupSet)



}