package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.PeerReviewerAllocationEditUIState
import com.ustadmobile.lib.db.entities.AssignmentSubmitterWithAllocations
import com.ustadmobile.lib.db.entities.PeerReviewerAllocation
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.DropDownOption
import com.ustadmobile.mui.components.UstadDropDownField
import csstype.px
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.*
import mui.system.Container
import mui.system.Stack
import mui.system.StackDirection
import react.FC
import react.Props
import react.ReactNode

external interface PeerReviewerAllocationEditProps: Props {
    var uiState: PeerReviewerAllocationEditUIState
    var onAssignRandomReviewerClick: () -> Unit
    var onAllocationClick: (AssignmentSubmitterWithAllocations) -> Unit
}

val PeerReviewerAllocationEditComponent2 = FC<PeerReviewerAllocationEditProps>{ props ->

    val strings = useStringsXml()

    Container{
        maxWidth = "lg"

        Stack{
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            Button {
                onClick = { props.onAssignRandomReviewerClick }
                variant = ButtonVariant.contained

                + strings[MessageID.assign_random_reviewers]
            }

            props.uiState.submitterListWithAllocations.forEachIndexed {    index, submitter ->

                val filteredList = props.uiState.submitterListWithAllocations.filter { it.name != submitter.name }
                val names = filteredList.map{
                    DropDownOption(it.name ?: "", it.name?: "")
                }

                Typography {
                    variant = TypographyVariant.body1
                    + submitter.name
                    sx {
                        paddingTop = 16.px
                        paddingLeft = 16.px
                    }
                }
                submitter.allocations?.forEach {    allocation ->
                    List{
                        ListItem{
                            ListItemSecondaryAction {

                                sx {
                                    width = 150.px
                                }

                                UstadDropDownField {
                                    value = ""
                                    label = names.first().label
                                    options = names
                                    itemLabel = { ReactNode((it as? DropDownOption)?.label ?: "") }
                                    itemValue = { (it as? DropDownOption)?.value ?: "" }
                                    onChange = {
                                        props.onAllocationClick(submitter.shallowCopy {
                                            name = it.toString()
                                        })
                                    }
                                }
                            }
                            ListItemText {
                                + strings[MessageID.reviewer].replace("%1\$s",
                                    (index+1).toString())
                            }
                        }
                    }
                }
            }
        }
    }

}

val PeerReviewerAllocationEditPreview = FC<Props> {
    PeerReviewerAllocationEditComponent2{
        uiState = PeerReviewerAllocationEditUIState(
            submitterListWithAllocations = listOf(
                AssignmentSubmitterWithAllocations().apply {
                    name = "Maryam"
                    submitterUid = 3
                    allocations = listOf(
                        PeerReviewerAllocation().apply {
                            praUid = 380
                        },
                        PeerReviewerAllocation().apply {
                            praUid = 400
                        }
                    )
                },
                AssignmentSubmitterWithAllocations().apply {
                    name = "Ahmad"
                    submitterUid = 1
                    allocations = listOf(
                        PeerReviewerAllocation().apply {
                            praUid = 38
                        },
                        PeerReviewerAllocation().apply {
                            praUid = 40
                        }
                    )
                },
                AssignmentSubmitterWithAllocations().apply {
                    name = "Intelligent Students"
                    submitterUid = 2
                    allocations = listOf(
                        PeerReviewerAllocation().apply {
                            praUid = 99
                        },
                        PeerReviewerAllocation().apply {
                            praUid = 23
                        }
                    )
                }
            ),
        )
    }
}