package com.ustadmobile.libuicompose.view.clazzassignment.peerreviewerallocationedit

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.clazzassignment.peerreviewerallocationedit.PeerReviewerAllocationEditUIState
import com.ustadmobile.lib.db.entities.AssignmentSubmitterAndAllocations
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary
import com.ustadmobile.lib.db.entities.PeerReviewerAllocation

@Composable
@Preview
fun PeerReviewerAllocationEditPreview(){
    PeerReviewerAllocationEditScreen(
        uiState = PeerReviewerAllocationEditUIState(
            submitterListWithAllocations = listOf(
                AssignmentSubmitterAndAllocations(
                    submitter = AssignmentSubmitterSummary(
                        name = "Maryam",
                        submitterUid = 1
                    ),
                    allocations = listOf(
                        PeerReviewerAllocation().apply {
                            praUid = 321
                            praMarkerSubmitterUid = 0
                        },
                        PeerReviewerAllocation().apply {
                            praUid = 2131
                            praMarkerSubmitterUid = 3
                        }
                    )
                ),
                AssignmentSubmitterAndAllocations(
                    submitter = AssignmentSubmitterSummary(
                        name = "Ahmad",
                        submitterUid = 2
                    ),
                    allocations = listOf(
                        PeerReviewerAllocation().apply {
                            praUid = 134
                            praMarkerSubmitterUid = 3
                        },
                        PeerReviewerAllocation().apply {
                            praUid = 31321
                            praMarkerSubmitterUid = 1
                        }
                    )
                ),
                AssignmentSubmitterAndAllocations(
                    submitter = AssignmentSubmitterSummary(
                        name = "Intelligent Students",
                        submitterUid = 3
                    ),
                    allocations = listOf(
                        PeerReviewerAllocation().apply {
                            praUid = 13131
                            praMarkerSubmitterUid = 1
                        },
                        PeerReviewerAllocation().apply {
                            praUid = 23131
                            praMarkerSubmitterUid = 2
                        }
                    )
                )
            ),
        )
    )
}