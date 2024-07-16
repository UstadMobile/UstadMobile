package com.ustadmobile.core.domain.peerreviewallocation

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.PeerReviewerAllocation
import com.ustadmobile.lib.db.entities.ext.shallowCopy

/**
 * Peer assignment submission requires an allocation of the who marks who.
 */
class UpdatePeerReviewAllocationUseCase(
    private val db: UmAppDatabase,
    private val systemImpl: UstadMobileSystemImpl,
) {

    fun <T> List<T>.truncate(maxSize: Int): List<T> {
        return if(size <= maxSize)
            this
        else
            this.subList(0, maxSize)
    }

    inline fun <T> List<T>.padEnd(
        minSize: Int,
        element: (index: Int) -> T,
    ) : List<T> {
        return if(size >= minSize)
            this
        else
            this + ( 0 until (minSize - size)).map(element)
    }

    /**
     * @param groupUid If submitted by group, then the CourseGroupSet cgsUid. Otherwise 0 (individual submission)
     */
    suspend operator fun invoke(
        existingAllocations: List<PeerReviewerAllocation>,
        groupUid: Long,
        clazzUid: Long,
        assignmentUid: Long,
        numReviewsPerSubmission: Int,
        allocateRemaining: Boolean,
        resetAllocations: Boolean = false,
    ) : List<PeerReviewerAllocation>{
        val submitterUids = db.clazzAssignmentDao().getSubmitterUidsByClazzOrGroupSetUid(
            clazzUid = clazzUid,
            groupSetUid = groupUid,
            time = systemTimeInMillis(),
        )

        val allocationsForEachSubmitter: Map<Long, List<PeerReviewerAllocation>> = submitterUids.map { submitterToMarkUid ->
            val existingAllocationsForSubmitter = existingAllocations.filter {
                it.praToMarkerSubmitterUid == submitterToMarkUid
            }
            .truncate(numReviewsPerSubmission)
            .padEnd(numReviewsPerSubmission) {
                PeerReviewerAllocation().apply {
                    praUid = db.doorPrimaryKeyManager.nextIdAsync(PeerReviewerAllocation.TABLE_ID)
                    praToMarkerSubmitterUid = submitterToMarkUid
                    praAssignmentUid = assignmentUid
                }
            }

            submitterToMarkUid to existingAllocationsForSubmitter
        }.toMap()

        val allocationList = allocationsForEachSubmitter
            .flatMap { it.value }.toMutableList()
        if(resetAllocations) {
            for(index in allocationList.indices) {
                allocationList[index] = allocationList[index].copy(
                    praMarkerSubmitterUid = 0
                )
            }
        }

        if(allocateRemaining) {
            //put into bucket: each submitter uid n times, n = reviewsPerSubmission - count assignments
            //This is essentially like pulling names out of a hat, where fromBucket is the hat.
            val fromBucket = submitterUids.flatMap { submitterUid ->
                val numToMarkAlreadyAssignedToSubmitter = allocationList.count {
                    it.praMarkerSubmitterUid == submitterUid
                }

                (0 until (numReviewsPerSubmission - numToMarkAlreadyAssignedToSubmitter)).map {
                    submitterUid
                }
            }.shuffled().toMutableList()

            for(index in allocationList.indices) {
                val allocation = allocationList[index]
                if(allocation.praMarkerSubmitterUid == 0L) {
                    //Dont assign anyone to mark a single peer more than once
                    val otherMarkersForThisSubmitter = allocationList.filter {
                        it.praToMarkerSubmitterUid == allocation.praToMarkerSubmitterUid
                    }.map {
                        it.praMarkerSubmitterUid
                    }.filter { it != 0L }

                    val selectedMarkerUid = fromBucket.firstOrNull {
                        it != allocation.praToMarkerSubmitterUid &&
                            it !in otherMarkersForThisSubmitter
                    }?.also {
                        fromBucket.remove(it)
                    }

                    allocationList[index] = allocation.shallowCopy {
                        praMarkerSubmitterUid = selectedMarkerUid ?: 0
                    }
                }
            }
        }

        return allocationList
    }

}