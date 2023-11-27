package com.ustadmobile.libuicompose.view.clazzassignment.peerreviewerallocationedit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.clazzassignment.peerreviewerallocationedit.PeerReviewerAllocationEditUIState
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.clazzassignment.peerreviewerallocationedit.PeerReviewerAllocationEditViewModel
import com.ustadmobile.lib.db.entities.AssignmentSubmitterAndAllocations
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary
import com.ustadmobile.lib.db.entities.PeerReviewerAllocation
import com.ustadmobile.libuicompose.components.UstadEditHeader
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun PeerReviewerAllocationEditScreen(
    viewModel: PeerReviewerAllocationEditViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        PeerReviewerAllocationEditUIState(), Dispatchers.Main.immediate
    )

    PeerReviewerAllocationEditScreen(
        uiState = uiState,
        onAllocationChanged = viewModel::onAllocationChanged,
        onAssignRandomReviewerClick = viewModel::onAssignRandomReviewers
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeerReviewerAllocationEditScreen(
    uiState: PeerReviewerAllocationEditUIState,
    onAssignRandomReviewerClick: () -> Unit = {},
    onAllocationChanged: (PeerReviewerAllocation) -> Unit = {}
){
    val UNASSIGNED = AssignmentSubmitterAndAllocations(
        submitter = AssignmentSubmitterSummary(
            submitterUid = 0,
            name = "(${stringResource(MR.strings.unassigned)})"
        ),
        allocations = emptyList(),
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ){
        item {
            OutlinedButton(
                onClick = onAssignRandomReviewerClick,
                modifier = Modifier.fillMaxWidth().defaultItemPadding(),
            ) {
                Text(stringResource(MR.strings.assign_random_reviewers))
            }
        }

        uiState.submitterListWithAllocations.forEach { submitter ->
            val reviewerOptions = listOf(UNASSIGNED) + uiState.submitterListWithAllocations.filter {
                it.submitter.submitterUid != submitter.submitter.submitterUid
            }

            item(key = Pair(1, submitter.submitter.submitterUid)) {
                UstadEditHeader(submitter.submitter.name ?: "")
            }

            submitter.allocations.forEachIndexed { index, allocation ->
                item(key = Pair(2, allocation.praUid)) {
                    val allocatedName = reviewerOptions.firstOrNull {
                        it.submitter.submitterUid == allocation.praMarkerSubmitterUid
                    }?.submitter?.name ?: ""

                    var expanded by remember { mutableStateOf(false)}

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.menuAnchor()
                                .defaultItemPadding(start = 32.dp).fillMaxWidth(),
                            readOnly = true,
                            value = allocatedName,
                            label = {
                                Text(stringResource(MR.strings.reviewer).format(index + 1))
                            },
                            onValueChange = { },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false}
                        ) {
                            uiState.reviewerOptionsForAllocation(allocation).forEach { reviewer ->
                                DropdownMenuItem(
                                    text = { Text(reviewer.submitter.name ?: "") },
                                    onClick = {
                                        expanded = false
                                        onAllocationChanged(allocation.copy(
                                            praMarkerSubmitterUid = reviewer.submitter.submitterUid
                                        ))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

