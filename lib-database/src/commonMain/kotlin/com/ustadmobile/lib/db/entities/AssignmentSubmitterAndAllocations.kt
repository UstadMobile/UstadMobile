package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
data class AssignmentSubmitterAndAllocations(
    val submitter: AssignmentSubmitterSummary = AssignmentSubmitterSummary(),
    val allocations: List<PeerReviewerAllocation> = emptyList(),
)
