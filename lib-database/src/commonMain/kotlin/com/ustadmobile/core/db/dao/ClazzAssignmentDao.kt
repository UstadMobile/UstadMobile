package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.PersonWithAssignmentMetrics

@UmDao
@UmRepository
@Dao
abstract class ClazzAssignmentDao : BaseDao<ClazzAssignment> {

    @Query("SELECT * FROM ClazzAssignment WHERE clazzAssignmentUid = :clazzAssignmentUid " +
            "AND CAST(clazzAssignmentInactive AS INTEGER) = 0")
    abstract fun findByUidAsync(clazzAssignmentUid: Long): ClazzAssignment?

    @Update
    abstract suspend fun updateAsync(entity: ClazzAssignment): Int

    @Query("""SELECT * FROM ClazzAssignment WHERE 
        clazzAssignmentClazzUid = :clazzUid 
        AND CAST(clazzAssignmentInactive AS INTEGER) = 0
    """)
    abstract fun findByClazzUidFactory(clazzUid: Long): DataSource.Factory<Int, ClazzAssignment>

    @Query("""
        SELECT ClazzAssignment.*, 
         (SELECT COUNT(*) FROM ClazzMember WHERE clazzMemberClazzUid = :clazzUid AND
         CAST(clazzMemberActive AS INTEGER) = 0 AND clazzMemberRole = ${ClazzMember.ROLE_STUDENT} 
         ) as totalStudents,
          0 as startedStudents,
          0 as notStartedStudents,
          0 as completedStudents,
          "" as storiesTitle,
          (SELECT clazzAssignmentContentJoinContentUid FROM ClazzAssignmentContentJoin
          WHERE clazzAssignmentContentJoinClazzAssignmentUid = ClazzAssignment.clazzAssignmentUid
          ORDER BY clazzAssignmentContentJoinDateAdded ASC LIMIT 1) as firstContentEntryUid
         FROM ClazzAssignment
        LEFT JOIN Clazz ON Clazz.clazzUid = :clazzUid
        WHERE 
        clazzAssignmentClazzUid = :clazzUid AND CAST(clazzAssignmentInactive AS INTEGER) = 0
        
    """)
    abstract fun findWithMetricsByClazzUid(clazzUid: Long)
            : DataSource.Factory<Int, ClazzAssignmentWithMetrics>


    @Query(""" SELECT ClazzAssignment.*,
         0 as totalStudents, 
         0 as startedStudents, 
         0 as notStartedStudents, 
         0 as completedStudents,
         "" as storiesTitle, 
         (SELECT clazzAssignmentContentJoinContentUid FROM ClazzAssignmentContentJoin
          WHERE clazzAssignmentContentJoinClazzAssignmentUid = ClazzAssignment.clazzAssignmentUid
          ORDER BY clazzAssignmentContentJoinDateAdded ASC LIMIT 1) as firstContentEntryUid
         FROM ClazzAssignment WHERE clazzAssignmentUid = :clazzAssignmentUid 
        AND CAST(clazzAssignmentInactive AS INTEGER) = 0""")
    abstract fun findWithMetricByUidAsync(clazzAssignmentUid: Long): ClazzAssignmentWithMetrics?

    @Query("""
        SELECT Person.*, 
        0 as startedDate,
        0 as finishedDate,
        0 as percentageCompleted
        FROM ClazzAssignment
        LEFT JOIN Clazz ON Clazz.clazzUid = ClazzAssignment.clazzAssignmentClazzUid
        LEFT JOIN ClazzMember ON ClazzMember.clazzMemberClazzUid = Clazz.clazzUid 
            AND ClazzMember.clazzMemberRole = ${ClazzMember.ROLE_STUDENT}
            AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1
        LEFT JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid 
            AND CAST(Person.active AS INTEGER) = 1
        WHERE CAST(clazzAssignmentInactive AS INTEGER ) = 0 
            AND clazzAssignmentUid = :clazzAssignmentUid 
    """)
    abstract fun findAllStudentsInAssignmentWithMetrics(clazzAssignmentUid: Long)
        :DataSource.Factory<Int, PersonWithAssignmentMetrics>
}
