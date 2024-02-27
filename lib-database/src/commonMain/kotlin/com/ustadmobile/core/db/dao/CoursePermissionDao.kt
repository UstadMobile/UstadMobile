package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.cash.paging.PagingSource
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.HttpServerFunctionCall
import com.ustadmobile.door.annotation.HttpServerFunctionParam
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.composites.CoursePermissionAndListDetail
import com.ustadmobile.lib.db.entities.CoursePermission
import kotlinx.coroutines.flow.Flow

@DoorDao
@Repository
expect abstract class CoursePermissionDao {


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findByClazzUidAsPagingSource",
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "includeDeleted",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true",
                    )
                )
            )
        )
    )
    @Query("""
        SELECT CoursePermission.*, Person.*, PersonPicture.*
          FROM CoursePermission
               LEFT JOIN Person
                         ON Person.personUid = CoursePermission.cpToPersonUid
               LEFT JOIN PersonPicture
                         ON PersonPicture.personPictureUid = Person.personUid
         WHERE CoursePermission.cpClazzUid = :clazzUid 
           AND (CAST(:includeDeleted AS INTEGER) = 1 OR NOT CoursePermission.cpIsDeleted) 
    """)
    abstract fun findByClazzUidAsPagingSource(
        clazzUid: Long,
        includeDeleted: Boolean,
    ): PagingSource<Int, CoursePermissionAndListDetail>


    /**
     * ClazzUid parameter is added because ViewModel will check permission on the same clazzuid
     */
    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
    )
    @Query("""
        SELECT CoursePermission.*
          FROM CoursePermission
         WHERE CoursePermission.cpUid = :uid
           AND CoursePermission.cpClazzUid = :clazzUid
    """)
    abstract suspend fun findByUidAndClazzUid(uid: Long, clazzUid: Long): CoursePermission?

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
    )
    @Query("""
        SELECT CoursePermission.*
          FROM CoursePermission
         WHERE CoursePermission.cpUid = :uid
           AND CoursePermission.cpClazzUid = :clazzUid 
    """)
    abstract fun findByUidAndClazzUidAsFlow(uid: Long, clazzUid: Long): Flow<CoursePermission?>



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(coursePermission: CoursePermission)


    @Query("""
        UPDATE CoursePermission
           SET cpIsDeleted = :isDeleted,
               cpLastModified = :updateTime
         WHERE cpUid = :cpUid  
    """)
    abstract suspend fun setDeleted(cpUid: Long, isDeleted: Boolean, updateTime: Long)

}