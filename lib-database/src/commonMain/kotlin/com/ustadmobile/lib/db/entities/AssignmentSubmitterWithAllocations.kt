package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class AssignmentSubmitterWithAllocations : AssignmentSubmitterSummary() {

    var allocations: List<PeerReviewerAllocation>? = null

}