package com.ustadmobile.core.viewmodel.clazzassignment.peerreviewerallocationedit

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.AssignmentSubmitterAndAllocations
import com.ustadmobile.lib.db.entities.PeerReviewerAllocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.peerreviewallocation.UpdatePeerReviewAllocationUseCase
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.util.ext.padEnd
import com.ustadmobile.core.util.ext.replace
import com.ustadmobile.core.util.ext.trimToSize
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZ_ASSIGNMENT_UID
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary
import io.github.aakira.napier.Napier
import org.kodein.di.direct
import org.kodein.di.instance

data class PeerReviewerAllocationEditUIState(
    val submitterListWithAllocations: List<AssignmentSubmitterAndAllocations> = emptyList(),
) {

    /**
     * Get the list of potential reviewers for a given allocation. This will include all other
     * submitters except the one being marked and those already selected for the given submitter (
     * e.g. you cannot assign one submitter to review the same peer twice).
     */
    fun reviewerOptionsForAllocation(
        allocation: PeerReviewerAllocation
    ): List<AssignmentSubmitterAndAllocations> {
        val existingAllocationsForSubmitter = submitterListWithAllocations
            .firstOrNull {
                it.submitter.submitterUid == allocation.praToMarkerSubmitterUid
            }?.allocations ?: emptyList()

        val otherMarkersAlreadySelected = existingAllocationsForSubmitter.filter {
            it.praUid != allocation.praUid
        }.map { it.praMarkerSubmitterUid }

        return submitterListWithAllocations.filter {
            it.submitter.submitterUid != allocation.praToMarkerSubmitterUid &&
                it.submitter.submitterUid !in otherMarkersAlreadySelected
        }
    }
}

class PeerReviewerAllocationEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    private val updatePeerReviewAllocationUseCase: UpdatePeerReviewAllocationUseCase = UpdatePeerReviewAllocationUseCase(
        db = di.onActiveEndpoint().direct.instance(tag = DoorTag.TAG_DB),
        systemImpl = di.direct.instance(),
    )
): UstadEditViewModel(
    di, savedStateHandle, DEST_NAME
) {

    private val _uiState = MutableStateFlow(PeerReviewerAllocationEditUIState())

    val uiState: Flow<PeerReviewerAllocationEditUIState> = _uiState.asStateFlow()

    private val clazzUid = savedStateHandle[ARG_CLAZZUID]?.toLong() ?: 0

    private val numReviewersPerSubmitter = savedStateHandle[ARG_NUM_REVIEWERS_PER_SUBMITTER]?.toInt() ?: 1

    private val assignmentUid = savedStateHandle[ARG_CLAZZ_ASSIGNMENT_UID]?.toLong() ?: 0L

    private val groupUid = savedStateHandle[ARG_GROUP_SET_UID]?.toLong() ?: 0L

    init {
        _appUiState.update { prev ->
            prev.copy(title = systemImpl.getString(MR.strings.assign_reviewers))
        }

        viewModelScope.launch {
            val primaryKeyManager = activeDb.doorPrimaryKeyManager

            loadEntity(
                serializer = ListSerializer(AssignmentSubmitterAndAllocations.serializer()),
                onLoadFromDb = { db ->
                    val allocations = savedStateHandle.getJson(
                        key = ARG_ALLOCATIONS,
                        deserializer = ListSerializer(PeerReviewerAllocation.serializer())
                    )

                    //get list of members
                    val submittersAndNames = db
                        .clazzAssignmentDao().getSubmitterUidsAndNameByClazzOrGroupSetUid(
                            clazzUid = clazzUid,
                            groupSetUid = groupUid,
                            date = systemTimeInMillis(),
                            groupStr = systemImpl.getString(MR.strings.group)
                        )

                    Napier.d("Number of submitters: ${submittersAndNames.size}")

                    submittersAndNames.map { submitterAndName ->
                        AssignmentSubmitterAndAllocations(
                            submitter = AssignmentSubmitterSummary(
                                submitterUid = submitterAndName.submitterUid,
                                name = submitterAndName.name
                            ),
                            allocations = (allocations?.filter {
                                it.praToMarkerSubmitterUid == submitterAndName.submitterUid
                            } ?: emptyList())
                                .trimToSize(maxSize = numReviewersPerSubmitter)
                                .padEnd(
                                    minSize = numReviewersPerSubmitter,
                                    item = {
                                        PeerReviewerAllocation(
                                            praUid =  primaryKeyManager.nextIdAsync(PeerReviewerAllocation.TABLE_ID),
                                            praToMarkerSubmitterUid = submitterAndName.submitterUid,
                                            praAssignmentUid = assignmentUid
                                        )
                                    }
                                )

                        )
                    }
                },
                makeDefault = {
                    //will never happen
                    null
                },
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(submitterListWithAllocations = it ?: emptyList())
                    }
                }
            )

            _appUiState.update { prev ->
                prev.copy(
                    hideBottomNavigation = true,
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = systemImpl.getString(MR.strings.done),
                        onClick = this@PeerReviewerAllocationEditViewModel::onClickSave
                    )
                )
            }
        }
    }

    fun onAllocationChanged(allocation: PeerReviewerAllocation){
        _uiState.update { prev ->
            prev.copy(
                submitterListWithAllocations = prev.submitterListWithAllocations.map { submitterAndAllocations ->
                    if(submitterAndAllocations.submitter.submitterUid == allocation.praToMarkerSubmitterUid) {
                        submitterAndAllocations.copy(
                            allocations = submitterAndAllocations.allocations.replace(
                                element = allocation,
                                replacePredicate = {
                                    it.praUid == allocation.praUid
                                }
                            )
                        )
                    }else {
                        submitterAndAllocations
                    }
                }
            )
        }
    }

    fun onAssignRandomReviewers() {
        viewModelScope.launch {
            val newAllocations = updatePeerReviewAllocationUseCase(
                existingAllocations = _uiState.value.submitterListWithAllocations.flatMap {
                    it.allocations
                },
                groupUid = groupUid,
                clazzUid = clazzUid,
                assignmentUid = assignmentUid,
                numReviewsPerSubmission = numReviewersPerSubmitter,
                allocateRemaining = true,
                resetAllocations = true
            )

            _uiState.update { prev ->
                prev.copy(
                    submitterListWithAllocations = prev.submitterListWithAllocations.map { submitterAndAllocations ->
                        AssignmentSubmitterAndAllocations(
                            submitter = submitterAndAllocations.submitter,
                            allocations = newAllocations.filter {
                                it.praToMarkerSubmitterUid == submitterAndAllocations.submitter.submitterUid
                            }
                        )
                    }
                )
            }
        }
    }

    fun onClickSave() {
        finishWithResult(_uiState.value.submitterListWithAllocations.flatMap { it.allocations })
    }


    companion object {

        const val ARG_ALLOCATIONS = "allocations"

        const val ARG_GROUP_SET_UID = "groupSetUid"

        const val ARG_NUM_REVIEWERS_PER_SUBMITTER = "numReviewers"

        const val DEST_NAME = "PeerReviewerAllocationEdit"

    }

}