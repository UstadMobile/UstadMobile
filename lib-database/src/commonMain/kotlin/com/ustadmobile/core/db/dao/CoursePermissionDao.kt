package com.ustadmobile.core.db.dao

import androidx.room.Query
import app.cash.paging.PagingSource
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.composites.CoursePermissionAndListDetail

@DoorDao
@Repository
expect abstract class CoursePermissionDao {

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
    abstract fun findByClazzUid(
        clazzUid: Long,
        includeDeleted: Boolean,
    ): PagingSource<Int, CoursePermissionAndListDetail>


}