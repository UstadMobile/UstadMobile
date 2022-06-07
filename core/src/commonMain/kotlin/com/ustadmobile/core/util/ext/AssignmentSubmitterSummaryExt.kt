package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary
import com.ustadmobile.lib.db.entities.PeerReviewerAllocation

/**
 * reviewerCount - number of reviewers per submitter to assign
 * previousAllocations - list of peer allocations already assigned to each submitter
 *
 * @return map of submitters to new list of reviewers to assign to
 */
fun List<AssignmentSubmitterSummary>.assignRandomly(
    reviewerCount: Int,
    previousAllocation: List<PeerReviewerAllocation>? = null
): Map<Long, List<Long>> {

    val submitters = this

    val previousAllocationMap = previousAllocation?.groupBy { it.praToMarkerSubmitterUid }
        ?.mapValues { it.value.map { peer -> peer.praMarkerSubmitterUid } }
    val fromBucket = mutableListOf<Long>()
    val toBucket = mutableMapOf<Long, List<Long>>()
    // repeat every submitter based on number of reviews per person/group
    repeat(reviewerCount){
        fromBucket.addAll(submitters.map { it.submitterUid})
        fromBucket.shuffle()
    }

    repeat(reviewerCount){
        submitters.forEach {
                val toList = toBucket[it.submitterUid]?.toMutableList() ?: mutableListOf()
                val previousList = previousAllocationMap?.get(it.submitterUid) ?: listOf()
                val reviewerUid = fromBucket
                    .find { from ->
                        from != it.submitterUid && from !in toList && from !in previousList
                    } ?: 0L

                toList.add(reviewerUid)
                toBucket[it.submitterUid] = toList
                fromBucket.remove(reviewerUid)
        }
    }

    if(toBucket.values.flatten().contains(0)) {
        // TODO
    }

    return toBucket
}