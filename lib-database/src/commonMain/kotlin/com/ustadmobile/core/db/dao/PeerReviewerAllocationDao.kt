package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.PeerReviewerAllocation
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UserSession

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
        UPDATE PeerReviewerAllocation 
           SET praActive = :active, 
               praLct = :changeTime
         WHERE praUid = :cbUid""")
    abstract suspend fun updateActiveByUid(cbUid: Long, active: Boolean,  changeTime: Long)


}