package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.AssignmentFileSubmission

@Dao
@Repository
abstract class AssignmentFileSubmissionDao : BaseDao<AssignmentFileSubmission> {


    @Query("""
        SELECT * 
          FROM AssignmentFileSubmission
         WHERE afsAssignmentUid = :assignmentUid
           AND afsStudentUid = :studentUid
           AND afsActive
    """)
    abstract fun getAllFileSubmissionsFromStudent(assignmentUid: Long, studentUid: Long)
            : DoorDataSourceFactory<Int, AssignmentFileSubmission>

    @Query("""
        SELECT * 
          FROM AssignmentFileSubmission
         WHERE afsAssignmentUid = :assignmentUid
           AND afsStudentUid = :studentUid
           AND afsActive
           AND afsSubmitted
    """)
    abstract fun getAllSubmittedFileSubmissionsFromStudent(assignmentUid: Long, studentUid: Long)
            : DoorDataSourceFactory<Int, AssignmentFileSubmission>


    @Update
    abstract suspend fun updateAsync(entity: AssignmentFileSubmission): Int


    @Query("""
        UPDATE AssignmentFileSubmission
           SET afsSubmitted = :submit, afsLct = :currentTime,
               afsTimestamp = :currentTime
         WHERE afsAssignmentUid = :assignmentUid
           AND afsStudentUid = :studentUid
           AND afsActive 
           AND NOT afsSubmitted
    """)
    abstract suspend fun setFilesAsSubmittedForStudent(assignmentUid: Long, studentUid: Long,
                                                       submit: Boolean, currentTime: Long)


    @Query("""
        SELECT afsStudentUid
          FROM AssignmentFileSubmission
         WHERE afsAssignmentUid = :assignmentUid
           AND afsSubmitted 
           AND afsActive
         LIMIT 1
    """)
    abstract suspend fun findNextStudentToGrade(assignmentUid: Long): Long
}