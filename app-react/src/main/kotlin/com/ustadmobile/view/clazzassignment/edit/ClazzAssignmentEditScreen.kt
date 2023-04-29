package com.ustadmobile.view.clazzassignment.edit

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.*
import com.ustadmobile.core.viewmodel.clazzassignment.edit.ClazzAssignmentEditUiState
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditUiState
import com.ustadmobile.core.viewmodel.clazzassignment.ClazzAssignmentViewModelConstants.TextLimitType
import com.ustadmobile.hooks.courseTerminologyResource
import com.ustadmobile.hooks.useCourseTerminologyEntries
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzAssignment.Companion.COMPLETION_CRITERIA_GRADED
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.lib.db.entities.ext.shallowCopyWithEntity
import com.ustadmobile.mui.common.input
import com.ustadmobile.mui.common.readOnly
import com.ustadmobile.mui.components.UstadCourseBlockEdit
import com.ustadmobile.mui.components.UstadNumberTextField
import com.ustadmobile.view.components.UstadMessageIdSelectField
import com.ustadmobile.view.components.UstadSelectField
import com.ustadmobile.view.components.UstadSwitchField
import csstype.Cursor
import csstype.px
import js.core.jso
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.useState

external interface ClazzAssignmentEditScreenProps : Props {

    var uiState: ClazzAssignmentEditUiState

    var onChangeCourseBlockWithEntity: (CourseBlockWithEntity?) -> Unit

    //Used by embedded CourseBlockEdit
    var onChangeCourseBlock: (CourseBlock?) -> Unit

    var onClickSubmissionType: () -> Unit

    var onClickAssignReviewers: () -> Unit

}

val ClazzAssignmentEditScreenPreview = FC<Props> {

    var entity: CourseBlockWithEntity? by useState {
        CourseBlockWithEntity().apply {
            assignment = ClazzAssignment().apply {
                caMarkingType = ClazzAssignment.MARKED_BY_PEERS
            }
        }
    }

    ClazzAssignmentEditScreenComponent2 {
        uiState = ClazzAssignmentEditUiState(
            courseBlockEditUiState = CourseBlockEditUiState(
                courseBlock = CourseBlock().apply {
                    cbMaxPoints = 78
                    cbCompletionCriteria = COMPLETION_CRITERIA_GRADED
                },
                gracePeriodVisible = true,
                completionCriteriaOptions = ClazzAssignmentEditUiState.ASSIGNMENT_COMPLETION_CRITERIAS,
            ),
            minScoreVisible = true,
            entity = entity
        )

        onChangeCourseBlockWithEntity = {
            entity = it
        }

    }
}

private val ClazzAssignmentEditScreenComponent2 = FC<ClazzAssignmentEditScreenProps> { props ->

    val strings = useStringsXml()

    val terminologyEntries = useCourseTerminologyEntries(props.uiState.courseTerminology)

    Container {
        maxWidth = "lg"

        Stack {
            spacing = responsive(20.px)

            UstadCourseBlockEdit {
                uiState = props.uiState.courseBlockEditUiState
                onCourseBlockChange = props.onChangeCourseBlock
            }

            TextField {
                id = "cgsName"
                sx {
                    input {
                        cursor = Cursor.pointer
                    }
                }
                variant = FormControlVariant.outlined
                value = props.uiState.groupSet?.cgsName ?: strings[MessageID.individual]
                label = ReactNode(strings[MessageID.submission_type])
                disabled = !props.uiState.groupSetEnabled
                inputProps = jso {
                    readOnly = true
                }
                onClick = { props.onClickSubmissionType() }
            }

            UstadSwitchField {
                id = "caRequireFileSubmission"
                label = strings[MessageID.require_file_submission]
                checked = props.uiState.entity?.assignment?.caRequireFileSubmission ?: false
                onChanged = {
                    props.onChangeCourseBlockWithEntity(props.uiState.entity?.shallowCopyWithEntity {
                        assignment = props.uiState.entity?.assignment?.shallowCopy {
                            caRequireFileSubmission = it
                        }
                    })
                }
                enabled = props.uiState.fieldsEnabled
            }

            if (props.uiState.fileSubmissionVisible){
                UstadMessageIdSelectField {
                    id = "caFileType"
                    value = props.uiState.entity?.assignment?.caFileType ?: 0
                    label = strings[MessageID.file_type]
                    options = FileTypeConstants.FILE_TYPE_MESSAGE_IDS
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onChangeCourseBlockWithEntity(
                            props.uiState.entity?.shallowCopyWithEntity {
                                assignment = props.uiState.entity?.assignment?.shallowCopy {
                                    caFileType = it.value
                                }
                            })
                    }
                }

                UstadNumberTextField {
                    id = "caSizeLimit"
                    variant = FormControlVariant.outlined
                    value = (props.uiState.entity?.assignment?.caSizeLimit ?: 0).toFloat()
                    label = ReactNode(strings[MessageID.size_limit])
                    disabled = !props.uiState.fieldsEnabled
                    onChange = {
                        props.onChangeCourseBlockWithEntity(
                            props.uiState.entity?.shallowCopyWithEntity {
                                assignment = props.uiState.entity?.assignment?.shallowCopy {
                                    caSizeLimit = it.toInt()
                                }
                            })
                    }
                }

                UstadNumberTextField {
                    id = "caNumberOfFiles"
                    variant = FormControlVariant.outlined
                    value = (props.uiState.entity?.assignment?.caNumberOfFiles ?: 0).toFloat()
                    label = ReactNode(strings[MessageID.number_of_files])
                    disabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onChangeCourseBlockWithEntity(
                            props.uiState.entity?.shallowCopyWithEntity {
                                assignment = props.uiState.entity?.assignment?.shallowCopy {
                                    caNumberOfFiles = it.toInt()
                                }
                            })
                    }
                }
            }

            UstadSwitchField {
                id = "caRequireTextSubmission"
                label = strings[MessageID.require_text_submission]
                checked = props.uiState.entity?.assignment?.caRequireTextSubmission ?: false
                onChanged = {
                    props.onChangeCourseBlockWithEntity(props.uiState.entity?.shallowCopyWithEntity {
                        assignment = props.uiState.entity?.assignment?.shallowCopy {
                            caRequireTextSubmission = it
                        }
                    })
                }
                enabled = props.uiState.fieldsEnabled
            }

            if (props.uiState.textSubmissionVisible) {
                UstadSelectField<TextLimitType> {
                    id = "caTextLimitType"
                    options = TextLimitType.values().toList()
                    value = TextLimitType.values().firstOrNull {
                        it.value == props.uiState.entity?.assignment?.caTextLimitType
                    } ?: TextLimitType.LIMIT_WORDS
                    itemValue = { it.value.toString() }
                    itemLabel = { ReactNode(strings[it.messageId]) }
                    label = strings[MessageID.limit]
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onChangeCourseBlockWithEntity(
                            props.uiState.entity?.shallowCopyWithEntity {
                                assignment = props.uiState.entity?.assignment?.shallowCopy {
                                    caTextLimitType = it.value
                                }
                            })
                    }
                }

                UstadNumberTextField {
                    id = "caTextLimit"
                    value = (props.uiState.entity?.assignment?.caTextLimit ?: 0).toFloat()
                    label = ReactNode(strings[MessageID.maximum])
                    placeholder = strings[MessageID.maximum]
                    disabled = !props.uiState.fieldsEnabled

                    onChange = {
                        props.onChangeCourseBlockWithEntity(
                            props.uiState.entity?.shallowCopyWithEntity {
                                assignment = props.uiState.entity?.assignment?.shallowCopy {
                                    caTextLimit = it.toInt()
                                }
                            })
                    }
                }
            }

            UstadMessageIdSelectField {
                id = "caSubmissionPolicy"
                value = props.uiState.entity?.assignment?.caSubmissionPolicy ?: 0
                label = strings[MessageID.submission_policy]
                options = SubmissionPolicyConstants.SUBMISSION_POLICY_MESSAGE_IDS
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onChangeCourseBlockWithEntity(
                        props.uiState.entity?.shallowCopyWithEntity {
                            assignment = props.uiState.entity?.assignment?.shallowCopy {
                                caSubmissionPolicy = it.value
                            }
                        })
                }
            }

            UstadSelectField<Int> {
                id = "caMarkingType"
                value = (props.uiState.entity?.assignment?.caMarkingType
                    ?: ClazzAssignment.MARKED_BY_COURSE_LEADER)
                label = strings[MessageID.marked_by]
                options = listOf(ClazzAssignment.MARKED_BY_COURSE_LEADER, ClazzAssignment.MARKED_BY_PEERS)
                enabled = props.uiState.markingTypeEnabled
                itemLabel = { markingType ->
                    val messageId = MarkingTypeConstants.MARKING_TYPE_MESSAGE_IDS.first {
                        it.value == markingType
                    }.messageId
                    ReactNode(courseTerminologyResource(terminologyEntries, strings, messageId))
                }
                itemValue = { it.toString() }
                onChange = {
                    props.onChangeCourseBlockWithEntity(
                        props.uiState.entity?.shallowCopyWithEntity {
                            assignment = props.uiState.entity?.assignment?.shallowCopy {
                                caMarkingType = it
                            }
                        })
                }
            }

            if (props.uiState.peerMarkingVisible) {
                Stack {
                    direction = responsive(StackDirection.row)
                    spacing = responsive(10.px)

                    UstadNumberTextField {
                        id = "caPeerReviewerCount"
                        fullWidth = true
                        variant = FormControlVariant.outlined
                        value = (props.uiState.entity?.assignment?.caPeerReviewerCount ?: 0).toFloat()
                        label = ReactNode(strings[MessageID.reviews_per_user_group])
                        disabled = !props.uiState.fieldsEnabled
                        error = props.uiState.reviewerCountError != null
                        helperText = props.uiState.reviewerCountError?.let { ReactNode(it) }
                        onChange = {
                            props.onChangeCourseBlockWithEntity(
                                props.uiState.entity?.shallowCopyWithEntity{
                                    assignment = props.uiState.entity?.assignment?.shallowCopy {
                                        caPeerReviewerCount = it.toInt()
                                    }
                                })
                        }
                    }

                    Button {
                        id = "buttonAssignReviewers"
                        fullWidth = true
                        onClick = { props.onClickAssignReviewers() }
                        disabled = !props.uiState.fieldsEnabled
                        variant = ButtonVariant.contained
                        + strings[MessageID.assign_reviewers]
                    }
                }
            }

            UstadSwitchField {
                id = "caClassCommentEnabled"
                label = strings[MessageID.allow_class_comments]
                checked = props.uiState.entity?.assignment?.caClassCommentEnabled ?: false
                onChanged = {
                    props.onChangeCourseBlockWithEntity(props.uiState.entity?.shallowCopyWithEntity {
                        assignment = props.uiState.entity?.assignment?.shallowCopy {
                            caClassCommentEnabled = it
                        }
                    })
                }
                enabled = props.uiState.fieldsEnabled
            }

            UstadSwitchField {
                id = "caPrivateCommentsEnabled"
                label = strings[MessageID.allow_private_comments_from_students]
                checked = props.uiState.entity?.assignment?.caPrivateCommentsEnabled ?: false
                onChanged = {
                    props.onChangeCourseBlockWithEntity(props.uiState.entity?.shallowCopyWithEntity {
                        assignment = props.uiState.entity?.assignment?.shallowCopy {
                            caPrivateCommentsEnabled = it
                        }
                    })
                }
                enabled = props.uiState.fieldsEnabled
            }
        }
    }
}