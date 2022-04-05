package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.CourseGroupSet
import kotlin.js.JsName

@Repository
@Dao
abstract class CourseGroupSetDao : BaseDao<CourseGroupSet> {

    @Update
    abstract suspend fun updateAsync(entity: CourseGroupSet): Int

    @Query("""
        SELECT *
         FROM CourseGroupSet
        WHERE cgsActive
          AND cgsClazzUid = :clazzUid
     ORDER BY cgsName   
    """)
    abstract fun findAllCourseGroupSetForClazz(clazzUid: Long): DoorDataSourceFactory<Int, CourseGroupSet>


    @Query("""
        SELECT *
         FROM CourseGroupSet
        WHERE cgsActive
          AND cgsClazzUid = :clazzUid
     ORDER BY cgsName   
    """)
    abstract fun findAllCourseGroupSetForClazzList(clazzUid: Long): List<CourseGroupSet>

    @JsName("findByUid")
    @Query("""
        SELECT * 
         FROM CourseGroupSet 
        WHERE cgsUid = :uid
        """)
    abstract suspend fun findByUidAsync(uid: Long): CourseGroupSet?


}