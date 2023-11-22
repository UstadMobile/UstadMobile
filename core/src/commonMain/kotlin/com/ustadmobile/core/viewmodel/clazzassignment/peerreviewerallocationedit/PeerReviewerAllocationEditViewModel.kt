package com.ustadmobile.core.viewmodel.clazzassignment.peerreviewerallocationedit

import com.ustadmobile.lib.db.entities.AssignmentSubmitterAndAllocations

data class PeerReviewerAllocationEditUIState(
    val submitterListWithAllocations: List<AssignmentSubmitterAndAllocations> = emptyList()
)

class PeerReviewerAllocationEditViewModel {

    companion object {

        const val DEST_NAME = "PeerReviewerAllocationEdit"
    }

}