package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.PeerReviewerAllocation

@DoorDao
@Repository
expect abstract class PeerReviewerAllocationDao : BaseDao<PeerReviewerAllocation>, OneToManyJoinDao<PeerReviewerAllocation>{


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceListAsync(entries: List<PeerReviewerAllocation>)

    @Query("""
        SELECT *
         FROM PeerReviewerAllocation
        WHERE praAssignmentUid IN (:assignmentUid)
          AND praActive 
    """)
    abstract suspend fun getAllPeerReviewerAllocations(assignmentUid: List<Long>): List<PeerReviewerAllocation>

    @Query("""
        SELECT PeerReviewerAllocation.*
          FROM PeerReviewerAllocation
         WHERE PeerReviewerAllocation.praAssignmentUid IN
               (SELECT CourseBlock.cbEntityUid
                  FROM CourseBlock
                 WHERE CourseBlock.cbClazzUid = :clazzUid
                   AND CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
                   AND (CAST(:includeInactive AS INTEGER) = 1 OR CourseBlock.cbActive))
           AND (CAST(:includeInactive AS INTEGER) = 1 OR PeerReviewerAllocation.praActive)
    """)
    abstract suspend fun getAllPeerReviewerAllocationsByClazzUid(
        clazzUid: Long,
        includeInactive: Boolean
    ): List<PeerReviewerAllocation>

    @Query("""
        UPDATE PeerReviewerAllocation 
           SET praActive = :active, 
               praLct = :changeTime
         WHERE praUid = :cbUid""")
    abstract suspend fun updateActiveByUid(cbUid: Long, active: Boolean,  changeTime: Long)


}