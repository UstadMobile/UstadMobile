package com.ustadmobile.core.db.dao.respect

import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import androidx.room.Query
import app.cash.paging.PagingSource
import com.ustadmobile.lib.db.entities.respect.RespectAssignment

@DoorDao
@Repository
expect abstract class RespectAssignmentDao {

    @Query("""
        SELECT RespectAssignment.*
          FROM RespectAssignment
         WHERE EXISTS(
               SELECT 1
                 FROM ClazzEnrolment
                WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :accountPersonUid
                  AND ClazzEnrolment.clazzEnrolmentClazzUid = RespectAssignment.razToClazzUid)
    """)
    abstract fun findByPersonUid(accountPersonUid: Long): PagingSource<Int, RespectAssignment>

}