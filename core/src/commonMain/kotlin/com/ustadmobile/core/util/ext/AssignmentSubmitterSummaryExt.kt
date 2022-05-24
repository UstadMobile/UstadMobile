package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary

fun List<AssignmentSubmitterSummary>.assignRandomly(reviewerCount: Int): Map<Long, List<Long>> {

    val submitters = this

    val fromBucket = mutableListOf<Long>()
    val toBucket = mutableMapOf<Long, List<Long>>()
    // repeat every submitter based on number of reviews per person/group
    repeat(reviewerCount){
        fromBucket.addAll(submitters.map { it.submitterUid})
        fromBucket.shuffle()
    }

    repeat(reviewerCount){
        submitters.forEach{
            val toList = toBucket[it.submitterUid]?.toMutableList() ?: mutableListOf()
            val reviewerUid = fromBucket
                .find { from ->
                    from != it.submitterUid && from !in toList
                } ?: 0L

            toList.add(reviewerUid)
            toBucket[it.submitterUid] = toList
            fromBucket.remove(reviewerUid)
        }
    }

    return toBucket
}