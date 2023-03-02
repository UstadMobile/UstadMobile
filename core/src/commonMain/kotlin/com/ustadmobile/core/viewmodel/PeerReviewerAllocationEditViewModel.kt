package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.AssignmentSubmitterWithAllocations
import com.ustadmobile.lib.db.entities.PeerReviewerAllocation

data class PeerReviewerAllocationEditUIState(
    val submitterListWithAllocations: List<AssignmentSubmitterWithAllocations> = emptyList()
)