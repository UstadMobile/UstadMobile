package com.ustadmobile.view.clazzassignment.peerreviewerallocationedit

import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.clazzassignment.peerreviewerallocationedit.PeerReviewerAllocationEditUIState
import com.ustadmobile.lib.db.entities.AssignmentSubmitterAndAllocations
import com.ustadmobile.lib.db.entities.PeerReviewerAllocation
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.*
import mui.system.Stack
import mui.system.StackDirection
import react.FC
import react.Props
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.clazzassignment.peerreviewerallocationedit.PeerReviewerAllocationEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadStandardContainer
import react.useRequiredContext
import web.cssom.Color
import web.cssom.px

external interface PeerReviewerAllocationEditProps: Props {
    var uiState: PeerReviewerAllocationEditUIState
    var onAssignRandomReviewerClick: () -> Unit
    var onAllocationChanged: (PeerReviewerAllocation) -> Unit
}

val PeerReviewerAllocationEditComponent2 = FC<PeerReviewerAllocationEditProps>{ props ->

    val strings = useStringProvider()
    val theme by useRequiredContext(ThemeContext)


    UstadStandardContainer {
        maxWidth = "lg"

        Stack{
            direction = responsive(StackDirection.column)
            spacing = responsive(2)

            Button {
                onClick = { props.onAssignRandomReviewerClick() }
                variant = ButtonVariant.contained

                + strings[MR.strings.assign_random_reviewers]
            }

            props.uiState.submitterListWithAllocations.forEach { submitter ->
                Typography {
                    variant = TypographyVariant.body1

                    + submitter.submitter.name
                    sx {
                        paddingTop = 16.px
                        paddingLeft = 16.px
                    }
                }

                submitter.allocations.forEachIndexed {  index, allocation ->
                    val selectId = "select_${submitter.submitter.submitterUid}"
                    mui.material.Box {
                        sx {
                            paddingLeft = theme.spacing(4)
                        }

                        FormControl {
                            fullWidth = true

                            InputLabel {
                                id = "${selectId}_label"
                                sx {
                                    backgroundColor = Color(theme.palette.background.default)
                                }
                                +strings.format(MR.strings.reviewer, (index + 1))
                            }

                            Select {
                                value = allocation.praMarkerSubmitterUid.toString()
                                id = selectId
                                fullWidth = true
                                onChange = { event, _ ->
                                    val selectedVal = ("" + event.target.value)
                                    props.onAllocationChanged(
                                        allocation.copy(
                                            praMarkerSubmitterUid = selectedVal.toLong()
                                        )
                                    )
                                }

                                MenuItem {
                                    value = "0"
                                    + "(${strings[MR.strings.unassigned]})"
                                }


                                props.uiState.reviewerOptionsForAllocation(allocation).forEach { reviewer ->
                                    MenuItem {
                                        value =  reviewer.submitter.submitterUid.toString()
                                        + (reviewer.submitter.name ?: "")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

val PeerReviewerAllocationEditScreen = FC<Props> { props ->
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        PeerReviewerAllocationEditViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(PeerReviewerAllocationEditUIState())

    PeerReviewerAllocationEditComponent2 {
        uiState = uiStateVal
        onAssignRandomReviewerClick = viewModel::onAssignRandomReviewers
        onAllocationChanged = viewModel::onAllocationChanged
    }
}

@Suppress("unused")
val PeerReviewerAllocationEditPreview = FC<Props> {
    PeerReviewerAllocationEditComponent2 {
        onAllocationChanged = { }
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
    }
}