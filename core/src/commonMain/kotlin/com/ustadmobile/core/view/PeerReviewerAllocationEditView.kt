package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.AssignmentSubmitterWithAllocations
import com.ustadmobile.lib.db.entities.PeerReviewerAllocationList


interface PeerReviewerAllocationEditView: UstadEditView<PeerReviewerAllocationList> {

    var submitterListWithAllocations: List<AssignmentSubmitterWithAllocations>?

    companion object {

        const val ARG_ASSIGNMENT_GROUP = "arg_assignment_groupUid"

        const val ARG_REVIEWERS_COUNT = "arg_reviewers_count"

        const val VIEW_NAME = "PeerReviewerAllocationEditEditView"

    }

}