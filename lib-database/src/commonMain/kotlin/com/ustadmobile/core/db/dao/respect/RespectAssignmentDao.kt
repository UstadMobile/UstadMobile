package com.ustadmobile.core.db.dao.respect

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import androidx.room.Query
import app.cash.paging.PagingSource
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.lib.db.entities.respect.RespectAssignment

@DoorDao
@Repository
expect abstract class RespectAssignmentDao {

    @HttpAccessible
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


    @HttpAccessible
    @Query("""
        SELECT RespectAssignment.*
          FROM RespectAssignment
         WHERE razUid = :razUid
    """)
    abstract suspend fun findByUidAsync(razUid: Long): RespectAssignment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(entity: RespectAssignment)

}